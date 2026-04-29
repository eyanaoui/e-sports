package com.esports.controllers.admin;

import com.esports.dao.TournamentDAO;
import com.esports.models.Tournament;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class TournamentFormController {
    @FXML private Label formTitle;
    @FXML private TextField nameField, gameField, maxTeamsField, prizeField;
    @FXML private TextArea descArea;
    @FXML private DatePicker startDatePicker, deadlinePicker; // Added deadlinePicker
    @FXML private ComboBox<String> formatCombo, statusCombo;
    @FXML private Button deleteBtn;

    // These match your FXML Error Labels to handle "Contrôle de Saisie" visually
    @FXML private Label nameError, gameError, formatError, maxTeamsError, dateError, deadlineError, prizeError, statusError, descError;

    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private Tournament currentTournament;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        formatCombo.getItems().addAll("single_elimination", "double_elimination", "round_robin");
        statusCombo.getItems().addAll("open", "in_progress", "completed", "cancelled");

        // Hide error labels by default
        hideErrors();
    }

    private void hideErrors() {
        Label[] errors = {nameError, gameError, formatError, maxTeamsError, dateError, deadlineError, prizeError, statusError, descError};
        for (Label l : errors) { if (l != null) { l.setVisible(false); l.setManaged(false); } }
    }

    public void setTournament(Tournament t) {
        this.currentTournament = t;
        if (t != null) {
            formTitle.setText("Edit Tournament");
            nameField.setText(t.getName());
            gameField.setText(t.getGame());
            descArea.setText(t.getDescription());
            formatCombo.setValue(t.getFormat());
            maxTeamsField.setText(String.valueOf(t.getMaxTeams()));
            prizeField.setText(t.getPrize());
            statusCombo.setValue(t.getStatus());
            if (t.getStartDate() != null) startDatePicker.setValue(LocalDate.parse(t.getStartDate().split(" ")[0]));
            deleteBtn.setVisible(true);
        } else {
            formTitle.setText("Add Tournament");
            deleteBtn.setVisible(false);
            statusCombo.setValue("open");
        }
    }

    public void setOnSuccess(Runnable onSuccess) { this.onSuccess = onSuccess; }

    @FXML
    private void handleSave() {
        // 1. Reset visual errors
        hideErrors();
        boolean isValid = true;

        // 2. Contrôle de Saisie (Validation)
        if (nameField.getText().trim().isEmpty()) { showErr(nameError); isValid = false; }
        if (gameField.getText().trim().isEmpty()) { showErr(gameError); isValid = false; }
        if (formatCombo.getValue() == null) { showErr(formatError); isValid = false; }
        if (statusCombo.getValue() == null) { showErr(statusError); isValid = false; }
        if (startDatePicker.getValue() == null) { showErr(dateError); isValid = false; }
        if (deadlinePicker.getValue() == null) { showErr(deadlineError); isValid = false; }
        if (descArea.getText().trim().isEmpty()) { showErr(descError); isValid = false; }

        int maxT = 0;
        try {
            maxT = Integer.parseInt(maxTeamsField.getText());
            if (maxT <= 0) throw new Exception();
        } catch (Exception e) {
            showErr(maxTeamsError);
            isValid = false;
        }

        // Stop execution if validation fails
        if (!isValid) return;

        // 3. Initialize Model
        if (currentTournament == null) {
            currentTournament = new Tournament();
        }

        // 4. Map Data to Model
        currentTournament.setName(nameField.getText().trim());
        currentTournament.setGame(gameField.getText().trim());
        currentTournament.setDescription(descArea.getText().trim());
        currentTournament.setFormat(formatCombo.getValue());
        currentTournament.setStatus(statusCombo.getValue());
        currentTournament.setPrize(prizeField.getText().trim());
        currentTournament.setMaxTeams(maxT);
        currentTournament.setStartDate(startDatePicker.getValue().toString());
        currentTournament.setRegistrationDeadline(deadlinePicker.getValue().toString());
        // Logic: End date is set to 1 day after start date to satisfy the NOT NULL DB constraint
        currentTournament.setEndDate(startDatePicker.getValue().plusDays(1).toString());

        // 5. Execute Database Operation
        try {
            if (currentTournament.getId() == 0) {
                tournamentDAO.add(currentTournament);
            } else {
                tournamentDAO.update(currentTournament);
            }

            // 6. Refresh Table and Close
            if (onSuccess != null) {
                onSuccess.run();
            }
            closeWindow();

        } catch (Exception e) {
            // Fallback alert if Database still rejects the save
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not save to database");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Helper method to make the error labels in your FXML visible
    private void showErr(Label l) {
        if (l != null) {
            l.setVisible(true);
            l.setManaged(true);
        }
    }

    @FXML private void handleCancel() { closeWindow(); }

    @FXML
    private void handleDelete() {
        if (currentTournament != null && currentTournament.getId() != 0) {
            tournamentDAO.delete(currentTournament.getId());
            if (onSuccess != null) onSuccess.run();
            closeWindow();
        }
    }

    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}