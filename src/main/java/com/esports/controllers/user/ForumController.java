package com.esports.controllers.user;

import com.esports.dao.SujetDao;
import com.esports.models.Sujet;
import com.esports.utils.ForumInputRules;
import com.esports.utils.ValidationHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.stream.Collectors;

public class ForumController {
    private static final int TITRE_MIN = 3;
    private static final int TITRE_MAX = 200;
    private static final int CONTENU_MIN = 5;
    private static final int CONTENU_MAX = 5000;

    @FXML private TextField titreField, searchField;
    @FXML private TextArea contenuArea;
    @FXML private Label titreErrorLabel, contenuErrorLabel;
    @FXML private VBox forumContainer;
    @FXML private Button saveTopicButton;

    private final SujetDao sujetDao = new SujetDao();
    private List<Sujet> allSujets;
    private Sujet editingSujet;

    @FXML
    public void initialize() {
        if (saveTopicButton != null) {
            saveTopicButton.setText("Save topic");
        }
        titreField.textProperty().addListener((o, a, b) -> clearTitreError());
        contenuArea.textProperty().addListener((o, a, b) -> clearContenuError());
        clearAllErrors();
        refreshForum();
    }

    private void refreshForum() {
        allSujets = sujetDao.getAll();
        displaySujets(allSujets);
    }

    private void displaySujets(List<Sujet> sujets) {
        forumContainer.getChildren().clear();
        for (Sujet s : sujets) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: rgba(43, 43, 43, 0.9); -fx-padding: 10; -fx-background-radius: 5;");
            Label t = new Label(s.getTitre());
            t.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            t.setWrapText(true);

            HBox actions = new HBox(8);
            Button openBtn = new Button("Open");
            openBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            openBtn.setOnAction(e -> openMessages(s));

            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
            editBtn.setOnAction(e -> startEdit(s));

            Button delBtn = new Button("Delete");
            delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            delBtn.setOnAction(e -> confirmDelete(s));

            actions.getChildren().addAll(openBtn, editBtn, delBtn);
            card.getChildren().addAll(t, actions);
            forumContainer.getChildren().add(card);
        }
    }

    private void startEdit(Sujet s) {
        editingSujet = sujetDao.getById(s.getId());
        if (editingSujet == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Topic");
            alert.setHeaderText(null);
            alert.setContentText("Topic not found.");
            alert.showAndWait();
            return;
        }
        titreField.setText(editingSujet.getTitre());
        contenuArea.setText(editingSujet.getContenu() != null ? editingSujet.getContenu() : "");
        if (saveTopicButton != null) {
            saveTopicButton.setText("Update topic");
        }
    }

    @FXML
    private void handleClearForm() {
        editingSujet = null;
        titreField.clear();
        contenuArea.clear();
        clearAllErrors();
        if (saveTopicButton != null) {
            saveTopicButton.setText("Save topic");
        }
    }

    @FXML
    private void handleSaveTopic() {
        if (!validateSujetInputs()) {
            return;
        }
        String titre = titreField.getText().trim();
        String contenu = contenuArea.getText().trim();
        if (editingSujet == null) {
            Sujet s = new Sujet();
            s.setTitre(titre);
            s.setContenu(contenu);
            sujetDao.add(s);
        } else {
            editingSujet.setTitre(titre);
            editingSujet.setContenu(contenu);
            sujetDao.update(editingSujet);
        }
        handleClearForm();
        refreshForum();
    }

    private void confirmDelete(Sujet s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete topic");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete this topic and all its messages?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sujetDao.delete(s.getId());
                if (editingSujet != null && editingSujet.getId() == s.getId()) {
                    handleClearForm();
                }
                refreshForum();
            }
        });
    }

    @FXML
    private void handleSearch() {
        String raw = searchField.getText();
        if (raw == null || raw.trim().isEmpty()) {
            displaySujets(allSujets);
            return;
        }
        String query = raw.toLowerCase();
        List<Sujet> filtered = allSujets.stream()
                .filter(x -> x.getTitre() != null && x.getTitre().toLowerCase().contains(query))
                .collect(Collectors.toList());
        displaySujets(filtered);
    }

    private boolean validateSujetInputs() {
        clearAllErrors();
        String titre = titreField.getText() != null ? titreField.getText().trim() : "";
        String contenu = contenuArea.getText() != null ? contenuArea.getText().trim() : "";
        boolean valid = true;

        String titreErr = ForumInputRules.validateTopicTitle(titre, TITRE_MIN, TITRE_MAX);
        if (titreErr != null) {
            showTitreError(titreErr);
            valid = false;
        }

        String contenuErr = ForumInputRules.validateTopicContent(contenu, CONTENU_MIN, CONTENU_MAX);
        if (contenuErr != null) {
            showContenuError(contenuErr);
            valid = false;
        }
        return valid;
    }

    private void openMessages(Sujet s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/MessageView.fxml"));
            Parent root = loader.load();
            MessageController controller = loader.getController();
            controller.setSujet(s);
            Stage stage = new Stage();
            stage.setTitle("Messages — " + s.getTitre());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearAllErrors() {
        clearTitreError();
        clearContenuError();
    }

    private void showTitreError(String msg) {
        ValidationHelper.setFieldError(titreField, true);
        setErrorLabel(titreErrorLabel, msg);
    }

    private void showContenuError(String msg) {
        ValidationHelper.setFieldError(contenuArea, true);
        setErrorLabel(contenuErrorLabel, msg);
    }

    private void clearTitreError() {
        ValidationHelper.clearFieldError(titreField);
        setErrorLabel(titreErrorLabel, null);
    }

    private void clearContenuError() {
        ValidationHelper.clearFieldError(contenuArea);
        setErrorLabel(contenuErrorLabel, null);
    }

    private void setErrorLabel(Label label, String msg) {
        if (label == null) return;
        boolean show = msg != null && !msg.isBlank();
        label.setText(show ? msg : "");
        label.setVisible(show);
        label.setManaged(show);
    }
}
