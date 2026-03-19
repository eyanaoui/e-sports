package com.esports.controllers.user;

import com.esports.dao.GuideRatingDAO;
import com.esports.dao.GuideStepDAO;
import com.esports.models.Guide;
import com.esports.models.GuideRating;
import com.esports.models.GuideStep;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.FileInputStream;
import java.util.List;

public class GuideDetailController {

    @FXML private Label guideTitleLabel, guideDescLabel, difficultyLabel;
    @FXML private VBox stepsContainer, ratingsContainer;
    @FXML private ComboBox<Integer> ratingBox;
    @FXML private TextField commentField;

    private GuideStepDAO stepDAO     = new GuideStepDAO();
    private GuideRatingDAO ratingDAO = new GuideRatingDAO();
    private Guide currentGuide;

    @FXML
    public void initialize() {
        ratingBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
    }

    public void setGuide(Guide guide) {
        this.currentGuide = guide;

        guideTitleLabel.setText(guide.getTitle());
        guideDescLabel.setText(guide.getDescription() != null ? guide.getDescription() : "");

        String diffColor = switch (guide.getDifficulty()) {
            case "Easy"   -> "#2ecc71";
            case "Medium" -> "#f39c12";
            case "Hard"   -> "#e74c3c";
            default       -> "#999";
        };
        difficultyLabel.setText(guide.getDifficulty());
        difficultyLabel.setStyle(
                "-fx-background-color: " + diffColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 2 8 2 8;" +
                        "-fx-background-radius: 99;" +
                        "-fx-font-size: 12;"
        );

        loadSteps();
        loadRatings();
    }

    private void loadSteps() {
        stepsContainer.getChildren().clear();
        List<GuideStep> steps = stepDAO.getByGuideId(currentGuide.getId());

        for (GuideStep step : steps) {
            VBox stepCard = new VBox(8);
            stepCard.setStyle(
                    "-fx-background-color: #f8f9fa;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 12;"
            );

            Label stepNum = new Label("Step " + step.getStepOrder() + " — " + step.getTitle());
            stepNum.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

            Label content = new Label(step.getContent());
            content.setWrapText(true);
            content.setStyle("-fx-font-size: 12; -fx-text-fill: #444;");

            stepCard.getChildren().addAll(stepNum, content);

            // show thumbnail image
            if (step.getImage() != null && !step.getImage().isEmpty()) {
                try {
                    String imagePath = "target/classes/images/" + step.getImage();
                    Image img = new Image(new FileInputStream(imagePath));
                    ImageView imageView = new ImageView(img);
                    imageView.setFitWidth(300);
                    imageView.setFitHeight(180);
                    imageView.setPreserveRatio(true);
                    stepCard.getChildren().add(imageView);
                } catch (Exception e) {
                    System.out.println("❌ Step image not found: " + e.getMessage());
                }
            }

            // clickable video link
            if (step.getVideoUrl() != null && !step.getVideoUrl().isEmpty()) {
                Hyperlink video = new Hyperlink("▶ Watch Video");
                video.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12;");
                video.setOnAction(e -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(
                                new java.net.URI(step.getVideoUrl()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                stepCard.getChildren().add(video);
            }

            stepsContainer.getChildren().add(stepCard);
        }

        if (steps.isEmpty()) {
            stepsContainer.getChildren().add(
                    new Label("No steps available for this guide.")
            );
        }
    }

    private void loadRatings() {
        ratingsContainer.getChildren().clear();
        List<GuideRating> ratings = ratingDAO.getByGuideId(currentGuide.getId());
        for (GuideRating rating : ratings) {
            HBox row = new HBox(10);
            row.setStyle(
                    "-fx-background-color: #f8f9fa;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 10;"
            );

            String stars = "⭐".repeat(rating.getRatingValue());
            Label starLabel = new Label(stars);
            starLabel.setStyle("-fx-font-size: 13;");

            Label comment = new Label(rating.getComment() != null ?
                    rating.getComment() : "No comment");
            comment.setWrapText(true);
            comment.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

            row.getChildren().addAll(starLabel, comment);
            ratingsContainer.getChildren().add(row);
        }

        if (ratings.isEmpty()) {
            ratingsContainer.getChildren().add(
                    new Label("No ratings yet. Be the first to rate!")
            );
        }
    }

    @FXML
    private void handleSubmitRating() {
        if (ratingBox.getValue() == null) {
            showAlert("Please select a rating!");
            return;
        }
        GuideRating rating = new GuideRating(
                currentGuide.getId(),
                1,
                ratingBox.getValue(),
                commentField.getText().trim()
        );
        ratingDAO.add(rating);
        commentField.clear();
        ratingBox.setValue(null);
        loadRatings();
        showAlert("Rating submitted! ⭐");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}