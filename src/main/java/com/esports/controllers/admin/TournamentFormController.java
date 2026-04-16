package com.esports.controllers.admin;

import com.esports.dao.TournamentDAO;
import com.esports.models.Tournament;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class TournamentFormController {
    @FXML private Label formTitle;
    @FXML private TextField nameField, gameField, maxTeamsField, prizeField;
    @FXML private ComboBox<String> formatCombo, statusCombo;
    @FXML private DatePicker startDatePicker, deadlinePicker;
    @FXML private TextArea descArea;
    @FXML private Button deleteBtn;

    // Error Labels
    @FXML private Label nameError, gameError, formatError, maxTeamsError, dateError, deadlineError, prizeError, statusError, descError;

    private final TournamentDAO dao = new TournamentDAO();
    private Tournament currentTournament;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        formatCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                "Single Elimination", "Double Elimination", "Round Robin", "League"
        ));
        statusCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                "Planned", "Ongoing", "Finished", "Cancelled"
        ));
        clearErrors();
    }

    private void clearErrors() {
        Label[] labels = {nameError, gameError, formatError, maxTeamsError, dateError, deadlineError, prizeError, statusError, descError};
        for (Label l : labels) {
            if (l != null) {
                l.setVisible(false);
                l.setManaged(false);
            }
        }
    }

    private void showFieldError(Label label, String message) {
        if (label != null) {
            label.setText("⚠️ " + message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    public void setTournament(Tournament t) {
        this.currentTournament = t;
        if (t != null) {
            formTitle.setText("Edit Tournament");
            nameField.setText(t.getName());
            gameField.setText(t.getGame());
            maxTeamsField.setText(String.valueOf(t.getMax_teams()));
            prizeField.setText(t.getPrize());
            formatCombo.setValue(t.getFormat());
            statusCombo.setValue(t.getStatus());
            descArea.setText(t.getDescription());

            if (t.getStart_date() != null) startDatePicker.setValue(t.getStart_date().toLocalDate());
            if (t.getRegistration_deadline() != null) deadlinePicker.setValue(t.getRegistration_deadline().toLocalDate());

            deleteBtn.setVisible(true);
        } else {
            formTitle.setText("New Tournament");
            deleteBtn.setVisible(false);
        }
    }

    public void setOnSuccess(Runnable callback) { this.onSuccess = callback; }

    @FXML
    private void handleSave() {
        clearErrors();
        boolean isValid = true;

        // Validation Logic
        if (nameField.getText().trim().isEmpty()) { showFieldError(nameError, "Name is required"); isValid = false; }
        if (gameField.getText().trim().isEmpty()) { showFieldError(gameError, "Game is required"); isValid = false; }
        if (prizeField.getText().trim().isEmpty()) { showFieldError(prizeError, "Prize pool is required"); isValid = false; }
        if (descArea.getText().trim().isEmpty()) { showFieldError(descError, "Description is required"); isValid = false; }

        if (formatCombo.getValue() == null) { showFieldError(formatError, "Select a format"); isValid = false; }
        if (statusCombo.getValue() == null) { showFieldError(statusError, "Select a status"); isValid = false; }

        if (startDatePicker.getValue() == null) { showFieldError(dateError, "Start date is required"); isValid = false; }
        if (deadlinePicker.getValue() == null) { showFieldError(deadlineError, "Deadline is required"); isValid = false; }

        // Logic check: Deadline must be before start date
        if (startDatePicker.getValue() != null && deadlinePicker.getValue() != null) {
            if (deadlinePicker.getValue().isAfter(startDatePicker.getValue())) {
                showFieldError(deadlineError, "Deadline must be before start");
                isValid = false;
            }
        }

        try {
            int teams = Integer.parseInt(maxTeamsField.getText().trim());
            if (teams <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showFieldError(maxTeamsError, "Must be a positive number");
            isValid = false;
        }

        if (!isValid) return;

        // Create/Update Object
        Tournament t = (currentTournament == null) ? new Tournament() : currentTournament;
        t.setName(nameField.getText().trim());
        t.setGame(gameField.getText().trim());
        t.setFormat(formatCombo.getValue());
        t.setMax_teams(Integer.parseInt(maxTeamsField.getText().trim()));
        t.setPrize(prizeField.getText().trim());
        t.setStatus(statusCombo.getValue());
        t.setDescription(descArea.getText().trim());

        t.setStart_date(startDatePicker.getValue().atStartOfDay());
        t.setRegistration_deadline(deadlinePicker.getValue().atStartOfDay());
        t.setEnd_date(t.getStart_date().plusDays(1));
        t.setOrganizer_id(3);

        if (currentTournament == null) {
            dao.add(t);
        } else {
            dao.update(t);
        }

        if (onSuccess != null) onSuccess.run();
        handleCancel();
    }

    @FXML
    private void handleDelete() {
        if (currentTournament != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Tournament: " + currentTournament.getName());
            alert.setContentText("This action is permanent. Do you want to proceed?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                dao.delete(currentTournament.getId());
                if (onSuccess != null) onSuccess.run();
                handleCancel();
            }
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}