package com.esports.controllers.admin;

import com.esports.dao.GuideDAO;
import com.esports.dao.GuideStepDAO;
import com.esports.models.Guide;
import com.esports.models.GuideStep;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class GuideStepFormController {

    @FXML private Label formTitle;
    @FXML private ComboBox<Guide> guideBox;
    @FXML private TextField titleField, orderField, imageField, videoField;
    @FXML private TextArea contentField;
    @FXML private Button deleteBtn;

    private GuideStepDAO stepDAO = new GuideStepDAO();
    private GuideDAO guideDAO    = new GuideDAO();
    private GuideStep currentStep;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        guideBox.setItems(FXCollections.observableArrayList(guideDAO.getAll()));
    }

    public void setStep(GuideStep step) {
        this.currentStep = step;
        if (step == null) {
            formTitle.setText("Add Step");
            deleteBtn.setVisible(false);
        } else {
            formTitle.setText("Edit Step");
            deleteBtn.setVisible(true);
            titleField.setText(step.getTitle());
            contentField.setText(step.getContent());
            orderField.setText(String.valueOf(step.getStepOrder()));
            imageField.setText(step.getImage() != null ? step.getImage() : "");
            videoField.setText(step.getVideoUrl() != null ? step.getVideoUrl() : "");
            guideBox.getItems().stream()
                    .filter(g -> g.getId() == step.getGuideId())
                    .findFirst()
                    .ifPresent(guideBox::setValue);
        }
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        File file = fileChooser.showOpenDialog((Stage) titleField.getScene().getWindow());
        if (file != null) imageField.setText(file.getName());
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) return;
        int order;
        try {
            order = Integer.parseInt(orderField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Step order must be a number!"); return;
        }
        if (currentStep == null) {
            GuideStep s = new GuideStep(
                    guideBox.getValue().getId(),
                    titleField.getText().trim(),
                    contentField.getText().trim(),
                    order,
                    imageField.getText().trim(),
                    videoField.getText().trim()
            );
            stepDAO.add(s);
        } else {
            currentStep.setGuideId(guideBox.getValue().getId());
            currentStep.setTitle(titleField.getText().trim());
            currentStep.setContent(contentField.getText().trim());
            currentStep.setStepOrder(order);
            currentStep.setImage(imageField.getText().trim());
            currentStep.setVideoUrl(videoField.getText().trim());
            stepDAO.update(currentStep);
        }
        if (onSuccess != null) onSuccess.run();
        closeWindow();
    }

    @FXML
    private void handleDelete() {
        if (currentStep == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Step");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this step?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stepDAO.delete(currentStep.getId());
                if (onSuccess != null) onSuccess.run();
                closeWindow();
            }
        });
    }

    @FXML
    private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    private boolean validateInputs() {
        if (guideBox.getValue() == null)              { showAlert("Please select a guide!"); return false; }
        if (titleField.getText().trim().isEmpty())     { showAlert("Title is required!"); return false; }
        if (contentField.getText().trim().isEmpty())   { showAlert("Content is required!"); return false; }
        if (orderField.getText().trim().isEmpty())     { showAlert("Step order is required!"); return false; }
        return true;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}