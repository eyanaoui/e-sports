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
import com.esports.AppState;
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
        boolean dark = AppState.isDarkMode();

        for (GuideStep step : steps) {
            VBox stepCard = new VBox(8);
            stepCard.setStyle(
                    "-fx-background-color: " + (dark ? "#1a1a2e" : "#f8f9fa") + ";" +
                            "-fx-border-color: "     + (dark ? "#2a2a4a" : "#e0e0e0") + ";" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 12;"
            );

            String textColor = dark ? "#e0e0e0" : "#333";
            String muteColor = dark ? "#a0a0b0" : "#444";

            Label stepNum = new Label("Step " + step.getStepOrder() + " — " + step.getTitle());
            stepNum.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: " + textColor + ";");

            Label content = new Label(step.getContent());
            content.setWrapText(true);
            content.setStyle("-fx-font-size: 12; -fx-text-fill: " + muteColor + ";");

            stepCard.getChildren().addAll(stepNum, content);

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
                    System.out.println("[ERROR] Step image not found: " + e.getMessage());
                }
            }

            if (step.getVideoUrl() != null && !step.getVideoUrl().isEmpty()) {
                Hyperlink video = new Hyperlink("▶ Watch Video");
                video.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12;");
                video.setOnAction(e -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(step.getVideoUrl()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                stepCard.getChildren().add(video);
            }

            stepsContainer.getChildren().add(stepCard);
        }

        if (steps.isEmpty()) {
            stepsContainer.getChildren().add(new Label("No steps available for this guide."));
        }
    }

    private void loadRatings() {
        ratingsContainer.getChildren().clear();
        List<GuideRating> ratings = ratingDAO.getByGuideId(currentGuide.getId());
        boolean dark = AppState.isDarkMode();

        for (GuideRating rating : ratings) {
            HBox row = new HBox(10);
            row.setStyle(
                    "-fx-background-color: " + (dark ? "#1a1a2e" : "#f8f9fa") + ";" +
                            "-fx-border-color: "     + (dark ? "#2a2a4a" : "#e0e0e0") + ";" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 10;"
            );

            String stars = "*".repeat(rating.getRatingValue());
            Label starLabel = new Label(stars + " stars");
            starLabel.setStyle("-fx-font-size: 13;");

            Label comment = new Label(rating.getComment() != null ?
                    rating.getComment() : "No comment");
            comment.setWrapText(true);
            comment.setStyle("-fx-font-size: 12; -fx-text-fill: " + (dark ? "#a0a0b0" : "#555") + ";");

            Label sentimentBadge = new Label("...");
            sentimentBadge.setStyle(
                    "-fx-background-color: #ccc;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 2 8 2 8;" +
                            "-fx-background-radius: 99;" +
                            "-fx-font-size: 11;"
            );

            if (rating.getComment() != null && !rating.getComment().isEmpty()) {
                new Thread(() -> {
                    try {
                        String json = "{\"comment\": \"" +
                                rating.getComment().replace("\"", "'") + "\"}";
                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                                json, okhttp3.MediaType.parse("application/json"));
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url("http://127.0.0.1:5000/predict/sentiment")
                                .post(body).build();
                        okhttp3.Response response = client.newCall(request).execute();
                        org.json.JSONObject result = new org.json.JSONObject(response.body().string());
                        String sentiment  = result.getString("sentiment");
                        double confidence = result.getDouble("confidence");

                        javafx.application.Platform.runLater(() -> {
                            if (sentiment.equals("positive")) {
                                sentimentBadge.setText("[POSITIVE] " + (int)(confidence*100) + "%");
                                sentimentBadge.setStyle(
                                        "-fx-background-color: #2ecc71;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 2 8 2 8;" +
                                                "-fx-background-radius: 99;" +
                                                "-fx-font-size: 11;"
                                );
                            } else {
                                sentimentBadge.setText("[NEGATIVE] " + (int)(confidence*100) + "%");
                                sentimentBadge.setStyle(
                                        "-fx-background-color: #e74c3c;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 2 8 2 8;" +
                                                "-fx-background-radius: 99;" +
                                                "-fx-font-size: 11;"
                                );
                            }
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> sentimentBadge.setText("N/A"));
                    }
                }).start();
            }

            row.getChildren().addAll(starLabel, comment, sentimentBadge);
            ratingsContainer.getChildren().add(row);
        }

        if (ratings.isEmpty()) {
            ratingsContainer.getChildren().add(new Label("No ratings yet. Be the first to rate!"));
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
        showAlert("Rating submitted!");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}