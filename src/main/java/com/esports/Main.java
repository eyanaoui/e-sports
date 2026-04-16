package com.esports;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Loads the main dashboard with the sidebar and charts
        Parent root = FXMLLoader.load(getClass().getResource("/views/admin/admin-dashboard.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Esports Management System");
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}