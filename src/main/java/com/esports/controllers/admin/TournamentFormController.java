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
        if (nameField.getText().isEmpty() || startDatePicker.getValue() == null) {
            return;
        }

        Tournament t = (currentTournament == null) ? new Tournament() : currentTournament;
        t.setName(nameField.getText());
        t.setGame(gameField.getText());
        t.setFormat(formatCombo.getValue());

        try {
            t.setMax_teams(Integer.parseInt(maxTeamsField.getText()));
        } catch (NumberFormatException e) {
            t.setMax_teams(0);
        }

        t.setPrize(prizeField.getText());
        t.setStatus(statusCombo.getValue());
        t.setDescription(descArea.getText());

        // Convert dates to LocalDateTime
        t.setStart_date(startDatePicker.getValue().atStartOfDay());
        if (deadlinePicker.getValue() != null) {
            t.setRegistration_deadline(deadlinePicker.getValue().atStartOfDay());
        }
        t.setEnd_date(t.getStart_date().plusDays(1)); // Logic for default end date
        t.setOrganizer_id(3); // Placeholder for currently logged-in admin

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
            dao.delete(currentTournament.getId());
            if (onSuccess != null) onSuccess.run();
            handleCancel();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}