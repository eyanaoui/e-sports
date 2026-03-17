package com.esports.controllers.user;

import com.esports.dao.GameDAO;
import com.esports.models.Game;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GameBrowseController {

    @FXML private TextField searchField;
    @FXML private FlowPane gamesContainer;

    private GameDAO gameDAO = new GameDAO();

    @FXML
    public void initialize() {
        loadGames(gameDAO.getAll());
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            loadGames(gameDAO.getAll());
            return;
        }
        List<Game> filtered = gameDAO.getAll().stream()
                .filter(g -> g.getName().toLowerCase().contains(query)
                        || (g.getDescription() != null &&
                        g.getDescription().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        loadGames(filtered);
    }

    private void loadGames(List<Game> games) {
        gamesContainer.getChildren().clear();
        for (Game game : games) {
            gamesContainer.getChildren().add(createGameCard(game));
        }
    }

    private VBox createGameCard(Game game) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 15;" +
                        "-fx-cursor: hand;"
        );
        card.setPrefWidth(200);

        // show image if available
        if (game.getCoverImage() != null && !game.getCoverImage().isEmpty()) {
            try {
                String imagePath = "target/classes/images/" + game.getCoverImage();
                System.out.println("🔍 Looking for image at: " + new File(imagePath).getAbsolutePath());
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        new java.io.FileInputStream(imagePath));
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(img);
                imageView.setFitWidth(170);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
                card.getChildren().add(imageView);
            } catch (Exception e) {
                System.out.println("❌ Image not found: " + e.getMessage());
            }            }


        Label name = new Label(game.getName());
        name.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        name.setWrapText(true);

        Label desc = new Label(game.getDescription() != null ?
                game.getDescription() : "No description");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        desc.setWrapText(true);
        desc.setMaxHeight(60);

        Label ranking = new Label(game.isHasRanking() ? "🏆 Ranked" : "🎮 Casual");
        ranking.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");

        card.getChildren().addAll(name, desc, ranking);
        card.setOnMouseClicked(e -> openGuidesBrowse(game));

        return card;
    }

    private void openGuidesBrowse(Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/guide-browse.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Guides — " + game.getName());
            stage.setScene(new Scene(loader.load(), 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            GuideBrowseController controller = loader.getController();
            controller.setGame(game);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}