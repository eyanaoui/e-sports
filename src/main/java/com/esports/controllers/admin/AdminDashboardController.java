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
        showUsers();
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
    public void showUsers() {
        currentView = "/views/admin/user-view.fxml";
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
    private void handleLogout() {
        try {
            // Clear the session
            AppState.clearSession();
            
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent loginView = loader.load();
            Scene loginScene = new Scene(loginView);
            
            // Apply the current theme to login screen
            if (isDarkMode) {
                loginScene.getStylesheets().add(getClass().getResource("/styles-dark.css").toExternalForm());
            } else {
                loginScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            }
            
            // Get the current stage and set the login scene
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setTitle("Esports Login");
            stage.setScene(loginScene);
            stage.setWidth(600);
            stage.setHeight(500);
            stage.centerOnScreen();
            
            System.out.println("[INFO] User logged out successfully");
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
        }
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
                darkModeBtn.setText("[LIGHT] Light Mode");
            } else {
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                darkModeBtn.setText("[DARK] Dark Mode");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] CSS file not found: " + e.getMessage());
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