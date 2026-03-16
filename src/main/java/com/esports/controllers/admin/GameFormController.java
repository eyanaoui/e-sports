package com.esports.controllers.admin;
import javafx.stage.FileChooser;
import java.io.File;
import com.esports.dao.GameDAO;
import com.esports.models.Game;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class GameFormController {

    @FXML private Label formTitle;
    @FXML private TextField nameField, slugField, coverField;
    @FXML private TextArea descField;
    @FXML private CheckBox rankingCheck;
    @FXML private Button deleteBtn;

    private GameDAO gameDAO = new GameDAO();
    private Game currentGame;
    private Runnable onSuccess;

    public void setGame(Game game) {
        this.currentGame = game;
        if (game == null) {
            formTitle.setText("Add Game");
            deleteBtn.setVisible(false);
        } else {
            formTitle.setText("Edit Game");
            deleteBtn.setVisible(true);
            nameField.setText(game.getName());
            slugField.setText(game.getSlug());
            descField.setText(game.getDescription());
            coverField.setText(game.getCoverImage() != null ? game.getCoverImage() : "");
            rankingCheck.setSelected(game.isHasRanking());
        }
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        Stage stage = (Stage) nameField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            coverField.setText(file.getName());
        }
    }


    @FXML
    private void handleSave() {
        if (!validateInputs()) return;
        if (currentGame == null) {
            Game g = new Game(
                    nameField.getText().trim(),
                    slugField.getText().trim(),
                    descField.getText().trim(),
                    coverField.getText().trim(),
                    rankingCheck.isSelected()
            );
            gameDAO.add(g);
        } else {
            currentGame.setName(nameField.getText().trim());
            currentGame.setSlug(slugField.getText().trim());
            currentGame.setDescription(descField.getText().trim());
            currentGame.setCoverImage(coverField.getText().trim());
            currentGame.setHasRanking(rankingCheck.isSelected());
            gameDAO.update(currentGame);
        }
        if (onSuccess != null) onSuccess.run();
        closeWindow();
    }

    @FXML
    private void handleDelete() {
        if (currentGame == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Game");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this game?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                gameDAO.delete(currentGame.getId());
                if (onSuccess != null) onSuccess.run();
                closeWindow();
            }
        });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty())    { showAlert("Name is required!"); return false; }
        if (nameField.getText().trim().length() < 2) { showAlert("Name must be at least 2 characters!"); return false; }
        if (slugField.getText().trim().isEmpty())     { showAlert("Slug is required!"); return false; }
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