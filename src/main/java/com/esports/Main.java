package com.esports;

import com.esports.controllers.SignatureOAuthController;
import com.esports.services.OAuthCallbackServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private OAuthCallbackServer oauthCallbackServer;

    @Override
    public void start(Stage stage) throws Exception {
        // Clear any existing session
        AppState.clearSession();
        
        // Start OAuth callback server (only if OAuth is configured)
        try {
            SignatureOAuthController oauthController = new SignatureOAuthController();
            // Check if OAuth is enabled before starting server
            if (oauthController.isOAuthEnabled()) {
                oauthCallbackServer = new OAuthCallbackServer(oauthController);
                oauthCallbackServer.start();
                System.out.println("[OAuth] Callback server started on port 8080");
            } else {
                System.out.println("[OAuth] OAuth not configured. Callback server not started.");
            }
        } catch (Exception e) {
            System.err.println("[OAuth] Failed to start OAuth callback server: " + e.getMessage());
            // Don't throw exception - allow app to start without OAuth
        }
        
        // Load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Esports Login");
        stage.setScene(scene);
        
        // Open in fullscreen/maximized mode
        stage.setMaximized(true);
        stage.setResizable(true);
        
        // Stop OAuth server when application closes
        stage.setOnCloseRequest(event -> {
            if (oauthCallbackServer != null) {
                oauthCallbackServer.stop();
            }
        });
        
        stage.show();
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    @Override
    public void stop() throws Exception {
        // Stop OAuth callback server when application exits
        if (oauthCallbackServer != null) {
            oauthCallbackServer.stop();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
