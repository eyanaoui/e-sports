package com.esports.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private AnchorPane contentArea;

    @FXML
    public void initialize() {
        showGames();
    }

    @FXML
    public void showGames() {
        loadView("/views/admin/game-view.fxml");
    }

    @FXML
    public void showGuides() {
        loadView("/views/admin/guide-view.fxml");
    }

    @FXML
    public void showSteps() {
        loadView("/views/admin/guide-step-view.fxml");
    }

    @FXML
    public void showRatings() {
        loadView("/views/admin/guide-rating-view.fxml");
    }

    @FXML
    public void showUserView() {
        loadView("/views/user/game-browse.fxml");
    }

    @FXML
    public void showStats() {
        loadView("/views/admin/stats-view.fxml");
    }

    private void loadView(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}