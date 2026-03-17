package com.esports.controllers.user;

import com.esports.dao.GuideDAO;
import com.esports.models.Game;
import com.esports.models.Guide;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GuideBrowseController {

    @FXML private Label gameTitleLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private FlowPane guidesContainer;

    private GuideDAO guideDAO = new GuideDAO();
    private Game currentGame;

    @FXML
    public void initialize() {
        difficultyFilter.setItems(FXCollections.observableArrayList(
                "All", "Easy", "Medium", "Hard"
        ));
        difficultyFilter.setValue("All");
    }

    public void setGame(Game game) {
        this.currentGame = game;
        gameTitleLabel.setText("Guides — " + game.getName());
        loadGuides();
    }

    private void loadGuides() {
        List<Guide> guides = guideDAO.getByGameId(currentGame.getId());
        applyFilters(guides);
    }

    @FXML
    private void handleSearch() { applyFilters(guideDAO.getByGameId(currentGame.getId())); }

    @FXML
    private void handleFilter() { applyFilters(guideDAO.getByGameId(currentGame.getId())); }

    private void applyFilters(List<Guide> guides) {
        String query      = searchField.getText().toLowerCase().trim();
        String difficulty = difficultyFilter.getValue();

        List<Guide> filtered = guides.stream()
                .filter(g -> query.isEmpty() || g.getTitle().toLowerCase().contains(query))
                .filter(g -> difficulty == null || difficulty.equals("All")
                        || g.getDifficulty().equals(difficulty))
                .collect(Collectors.toList());

        guidesContainer.getChildren().clear();
        for (Guide guide : filtered) {
            guidesContainer.getChildren().add(createGuideCard(guide));
        }
    }

    private VBox createGuideCard(Guide guide) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 15;" +
                        "-fx-cursor: hand;"
        );
        card.setPrefWidth(220);

        // show image if available
        if (guide.getCoverImage() != null && !guide.getCoverImage().isEmpty()) {
            try {
                String imagePath = "target/classes/images/" + guide.getCoverImage();
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        new java.io.FileInputStream(imagePath));
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(img);
                imageView.setFitWidth(190);
                imageView.setFitHeight(110);
                imageView.setPreserveRatio(true);
                card.getChildren().add(imageView);
            } catch (Exception e) {
                // no image, skip
            }
        }

        Label title = new Label(guide.getTitle());
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        title.setWrapText(true);

        Label desc = new Label(guide.getDescription() != null ?
                guide.getDescription() : "No description");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        desc.setWrapText(true);
        desc.setMaxHeight(60);

        String diffColor = switch (guide.getDifficulty()) {
            case "Easy"   -> "#2ecc71";
            case "Medium" -> "#f39c12";
            case "Hard"   -> "#e74c3c";
            default       -> "#999";
        };

        Label difficulty = new Label(guide.getDifficulty());
        difficulty.setStyle(
                "-fx-background-color: " + diffColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 2 8 2 8;" +
                        "-fx-background-radius: 99;" +
                        "-fx-font-size: 11;"
        );

        card.getChildren().addAll(title, desc, difficulty);
        card.setOnMouseClicked(e -> openGuideDetail(guide));

        return card;
    }

    private void openGuideDetail(Guide guide) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/guide-detail.fxml"));
            Stage stage = new Stage();
            stage.setTitle(guide.getTitle());
            stage.setScene(new Scene(loader.load(), 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            GuideDetailController controller = loader.getController();
            controller.setGuide(guide);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}