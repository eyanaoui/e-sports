package com.esports.controllers.admin;

import com.esports.AppState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private AnchorPane contentArea;
    @FXML private Button darkModeBtn;
    private boolean isDarkMode = false;
    private String currentView = "/views/admin/game-view.fxml";

    @FXML
    public void initialize() {
        showGames();
    }

    @FXML
    public void showTeams() {
        currentView = "/views/admin/team-view.fxml";
        loadView(currentView);
    }

    @FXML
    public void showGames() {
        currentView = "/views/admin/game-view.fxml";
        loadView(currentView);
    }

    @FXML
    public void showGuides() {
        currentView = "/views/admin/guide-view.fxml";
        loadView(currentView);
    }

    @FXML
    public void showSteps() {
        currentView = "/views/admin/guide-step-view.fxml";
        loadView(currentView);
    }

    @FXML
    public void showRatings() {
        currentView = "/views/admin/guide-rating-view.fxml";
        loadView(currentView);
    }

    @FXML
    public void showStats() {
        currentView = "/views/admin/stats-view.fxml";
        loadView(currentView);
    }

    @FXML
    public void showUserView() {
        currentView = "/views/user/game-browse.fxml";
        loadView(currentView);
    }

    @FXML
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        AppState.setDarkMode(isDarkMode);
        Scene scene = contentArea.getScene();
        scene.getStylesheets().clear();
        try {
            if (isDarkMode) {
                scene.getStylesheets().add(getClass().getResource("/styles-dark.css").toExternalForm());
                darkModeBtn.setText("☀️  Light Mode");
            } else {
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                darkModeBtn.setText("🌙  Dark Mode");
            }
        } catch (Exception e) {
            System.out.println("❌ CSS file not found: " + e.getMessage());
        }
        loadView(currentView);
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