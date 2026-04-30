package com.esports.controllers.admin;

import com.esports.dao.GameDAO;
import com.esports.dao.GuideDAO;
import com.esports.dao.GuideRatingDAO;
import com.esports.dao.UserDAO;
import com.esports.db.DatabaseConnection;
import com.esports.models.Game;
import com.esports.models.Guide;
import com.esports.models.GuideRating;
import com.esports.models.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatsController {

    @FXML private Label totalGamesLabel, totalGuidesLabel, totalRatingsLabel, avgRatingLabel;
    @FXML private Label totalUsersLabel, bannedUsersLabel, activeUsersLabel;
    @FXML private Label exportStatusLabel;
    @FXML private Button exportExcelButton;
    @FXML private VBox bannedUsersCard;
    @FXML private BarChart<String, Number> guidesPerGameChart;
    @FXML private PieChart difficultyChart;
    @FXML private TableView<GuideStats> leaderboardTable;
    @FXML private TableColumn<GuideStats, Number> colRank, colAvgRating, colTotal;
    @FXML private TableColumn<GuideStats, String> colTitle, colGame;

    private GameDAO gameDAO     = new GameDAO();
    private GuideDAO guideDAO   = new GuideDAO();
    private GuideRatingDAO ratingDAO = new GuideRatingDAO();
    private UserDAO userDAO     = new UserDAO();
    private Connection con = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {
        loadMetricCards();
        loadUserStatistics();
        loadGuidesPerGameChart();
        loadDifficultyChart();
        loadLeaderboard();
    }

    private void loadMetricCards() {
        int totalGames   = gameDAO.getAll().size();
        int totalGuides  = guideDAO.getAll().size();
        int totalRatings = ratingDAO.getAll().size();

        double avgRating = ratingDAO.getAll().stream()
                .mapToInt(GuideRating::getRatingValue)
                .average()
                .orElse(0.0);

        totalGamesLabel.setText(String.valueOf(totalGames));
        totalGuidesLabel.setText(String.valueOf(totalGuides));
        totalRatingsLabel.setText(String.valueOf(totalRatings));
        avgRatingLabel.setText(String.format("%.1f", avgRating));
    }

    /**
     * Load user statistics: total users, banned users, and active users.
     */
    private void loadUserStatistics() {
        List<User> allUsers = userDAO.getAll();
        
        // Total users
        int totalUsers = allUsers.size();
        
        // Banned users (where isBlocked = true)
        long bannedUsers = allUsers.stream()
                .filter(user -> user.getIsBlocked() != null && user.getIsBlocked())
                .count();
        
        // Active users (where isBlocked = false or null)
        long activeUsers = allUsers.stream()
                .filter(user -> user.getIsBlocked() == null || !user.getIsBlocked())
                .count();
        
        // Update labels
        totalUsersLabel.setText(String.valueOf(totalUsers));
        bannedUsersLabel.setText(String.valueOf(bannedUsers));
        activeUsersLabel.setText(String.valueOf(activeUsers));
    }

    private void loadGuidesPerGameChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Guides");
        for (Game game : gameDAO.getAll()) {
            int count = guideDAO.getByGameId(game.getId()).size();
            series.getData().add(new XYChart.Data<>(game.getName(), count));
        }
        guidesPerGameChart.getData().clear();
        guidesPerGameChart.getData().add(series);
    }

    private void loadDifficultyChart() {
        long easy   = guideDAO.getAll().stream().filter(g -> "Easy".equals(g.getDifficulty())).count();
        long medium = guideDAO.getAll().stream().filter(g -> "Medium".equals(g.getDifficulty())).count();
        long hard   = guideDAO.getAll().stream().filter(g -> "Hard".equals(g.getDifficulty())).count();

        difficultyChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Easy",   easy),
                new PieChart.Data("Medium", medium),
                new PieChart.Data("Hard",   hard)
        ));
    }

    private void loadLeaderboard() {
        colRank.setCellValueFactory(c -> new SimpleIntegerProperty(
                leaderboardTable.getItems().indexOf(c.getValue()) + 1));
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().title));
        colGame.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().game));
        colAvgRating.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().avgRating));
        colTotal.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().totalRatings));

        try {
            String sql = """
                SELECT g.title, ga.name as game_name,
                       ROUND(AVG(gr.rating_value), 2) as avg_rating,
                       COUNT(gr.id) as total_ratings
                FROM guide g
                JOIN game ga ON g.game_id = ga.id
                JOIN guide_rating gr ON gr.guide_id = g.id
                GROUP BY g.id
                ORDER BY avg_rating DESC
                LIMIT 10
                """;
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            var list = FXCollections.<GuideStats>observableArrayList();
            while (rs.next()) {
                list.add(new GuideStats(
                        rs.getString("title"),
                        rs.getString("game_name"),
                        rs.getDouble("avg_rating"),
                        rs.getInt("total_ratings")
                ));
            }
            leaderboardTable.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // inner class for leaderboard data
    public static class GuideStats {
        String title, game;
        double avgRating;
        int totalRatings;

        GuideStats(String title, String game, double avgRating, int totalRatings) {
            this.title = title;
            this.game = game;
            this.avgRating = avgRating;
            this.totalRatings = totalRatings;
        }
    }

    /**
     * Handle click on banned users card to show detailed list.
     */
    @FXML
    private void handleShowBannedUsers() {
        // Get all banned users
        List<User> bannedUsers = userDAO.getAll().stream()
                .filter(user -> user.getIsBlocked() != null && user.getIsBlocked())
                .toList();
        
        if (bannedUsers.isEmpty()) {
            // Show info dialog if no banned users
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Banned Users");
            alert.setHeaderText("No Banned Users");
            alert.setContentText("There are currently no banned users in the system.");
            alert.showAndWait();
            return;
        }
        
        // Create a custom dialog with table
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Banned Users List");
        dialog.setHeaderText("List of Banned Users (" + bannedUsers.size() + " total)");
        
        // Create table
        TableView<User> table = new TableView<>();
        table.setPrefWidth(700);
        table.setPrefHeight(400);
        
        // ID Column
        TableColumn<User, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        idCol.setPrefWidth(50);
        
        // First Name Column
        TableColumn<User, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFirstName()));
        firstNameCol.setPrefWidth(150);
        
        // Last Name Column
        TableColumn<User, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
        lastNameCol.setPrefWidth(150);
        
        // Email Column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        emailCol.setPrefWidth(250);
        
        // Roles Column
        TableColumn<User, String> rolesCol = new TableColumn<>("Roles");
        rolesCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoles().toString()));
        rolesCol.setPrefWidth(100);
        
        // Add columns to table
        table.getColumns().addAll(idCol, firstNameCol, lastNameCol, emailCol, rolesCol);
        
        // Add data to table
        table.setItems(FXCollections.observableArrayList(bannedUsers));
        
        // Create content with table and info label
        VBox content = new VBox(10);
        content.setPrefWidth(700);
        
        Label infoLabel = new Label("These users are currently banned and cannot access the platform.");
        infoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #e67e22; -fx-padding: 10;");
        
        content.getChildren().addAll(infoLabel, table);
        
        // Set content
        dialog.getDialogPane().setContent(content);
        
        // Add close button
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        
        // Show dialog
        dialog.showAndWait();
    }

    /**
     * Handle export to Excel button click.
     * Creates an Excel file with all statistics data.
     */
    @FXML
    private void handleExportToExcel() {
        try {
            // Show file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Statistics Report");
            
            // Generate default filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            fileChooser.setInitialFileName("Esports_Statistics_" + timestamp + ".xlsx");
            
            // Set extension filter
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            
            // Get stage from any component
            Stage stage = (Stage) exportExcelButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // Disable button during export
                exportExcelButton.setDisable(true);
                exportStatusLabel.setText("Exporting...");
                exportStatusLabel.setStyle("-fx-text-fill: #f39c12;");
                
                // Create Excel workbook
                exportToExcel(file);
                
                // Show success message
                exportStatusLabel.setText("✓ Exported successfully to: " + file.getName());
                exportStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                
                // Re-enable button
                exportExcelButton.setDisable(false);
                
                // Clear message after 5 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        javafx.application.Platform.runLater(() -> {
                            exportStatusLabel.setText("");
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            
        } catch (Exception e) {
            exportStatusLabel.setText("✗ Export failed: " + e.getMessage());
            exportStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
            exportExcelButton.setDisable(false);
            e.printStackTrace();
        }
    }

    /**
     * Export all statistics to Excel file.
     */
    private void exportToExcel(File file) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        
        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        
        // Sheet 1: Overview Statistics
        createOverviewSheet(workbook, headerStyle, dataStyle, titleStyle);
        
        // Sheet 2: User Statistics
        createUserStatisticsSheet(workbook, headerStyle, dataStyle, titleStyle);
        
        // Sheet 3: Guides per Game
        createGuidesPerGameSheet(workbook, headerStyle, dataStyle, titleStyle);
        
        // Sheet 4: Top Rated Guides
        createTopRatedGuidesSheet(workbook, headerStyle, dataStyle, titleStyle);
        
        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }
        
        workbook.close();
    }

    /**
     * Create overview statistics sheet.
     */
    private void createOverviewSheet(Workbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Overview");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Esports Platform - Statistics Overview");
        titleCell.setCellStyle(titleStyle);
        
        // Timestamp
        rowNum++;
        Row timestampRow = sheet.createRow(rowNum++);
        timestampRow.createCell(0).setCellValue("Generated:");
        timestampRow.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Empty row
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Metric");
        headerCell1.setCellStyle(headerStyle);
        
        org.apache.poi.ss.usermodel.Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("Value");
        headerCell2.setCellStyle(headerStyle);
        
        // Data rows
        addDataRow(sheet, rowNum++, "Total Games", totalGamesLabel.getText(), dataStyle);
        addDataRow(sheet, rowNum++, "Total Guides", totalGuidesLabel.getText(), dataStyle);
        addDataRow(sheet, rowNum++, "Total Ratings", totalRatingsLabel.getText(), dataStyle);
        addDataRow(sheet, rowNum++, "Average Rating", avgRatingLabel.getText(), dataStyle);
        addDataRow(sheet, rowNum++, "Total Users", totalUsersLabel.getText(), dataStyle);
        addDataRow(sheet, rowNum++, "Banned Users", bannedUsersLabel.getText(), dataStyle);
        addDataRow(sheet, rowNum++, "Active Users", activeUsersLabel.getText(), dataStyle);
        
        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * Create user statistics sheet with detailed user information.
     */
    private void createUserStatisticsSheet(Workbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("User Statistics");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("User Statistics Details");
        titleCell.setCellStyle(titleStyle);
        
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "First Name", "Last Name", "Email", "Status", "Roles"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        List<User> users = userDAO.getAll();
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            
            org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
            cell0.setCellValue(user.getId());
            cell0.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
            cell1.setCellValue(user.getFirstName());
            cell1.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
            cell2.setCellValue(user.getLastName());
            cell2.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
            cell3.setCellValue(user.getEmail());
            cell3.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
            String status = (user.getIsBlocked() != null && user.getIsBlocked()) ? "Banned" : "Active";
            cell4.setCellValue(status);
            cell4.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell5 = row.createCell(5);
            cell5.setCellValue(user.getRoles().toString());
            cell5.setCellStyle(dataStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create guides per game sheet.
     */
    private void createGuidesPerGameSheet(Workbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Guides per Game");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Guides per Game");
        titleCell.setCellStyle(titleStyle);
        
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Game");
        headerCell1.setCellStyle(headerStyle);
        
        org.apache.poi.ss.usermodel.Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("Number of Guides");
        headerCell2.setCellStyle(headerStyle);
        
        // Data rows
        for (Game game : gameDAO.getAll()) {
            int count = guideDAO.getByGameId(game.getId()).size();
            Row row = sheet.createRow(rowNum++);
            
            org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(0);
            cell1.setCellValue(game.getName());
            cell1.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(1);
            cell2.setCellValue(count);
            cell2.setCellStyle(dataStyle);
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * Create top rated guides sheet.
     */
    private void createTopRatedGuidesSheet(Workbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Top Rated Guides");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Top Rated Guides");
        titleCell.setCellStyle(titleStyle);
        
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Rank", "Guide Title", "Game", "Average Rating", "Total Ratings"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        int rank = 1;
        for (GuideStats stats : leaderboardTable.getItems()) {
            Row row = sheet.createRow(rowNum++);
            
            org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
            cell0.setCellValue(rank++);
            cell0.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
            cell1.setCellValue(stats.title);
            cell1.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
            cell2.setCellValue(stats.game);
            cell2.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
            cell3.setCellValue(stats.avgRating);
            cell3.setCellStyle(dataStyle);
            
            org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
            cell4.setCellValue(stats.totalRatings);
            cell4.setCellStyle(dataStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create header cell style.
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create data cell style.
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create title cell style.
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }

    /**
     * Helper method to add a data row.
     */
    private void addDataRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        
        org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(0);
        cell1.setCellValue(label);
        cell1.setCellStyle(style);
        
        org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(1);
        cell2.setCellValue(value);
        cell2.setCellStyle(style);
    }
}