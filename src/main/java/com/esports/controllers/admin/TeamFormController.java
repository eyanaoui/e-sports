package com.esports.controllers.admin;

import com.esports.dao.TeamDAO;
import com.esports.models.Team;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class TeamFormController {
    @FXML private Label formTitle;
    @FXML private TextField nameField, logoField, captainField;
    @FXML private TextArea descField;
    @FXML private Button deleteBtn;

    private TeamDAO teamDAO = new TeamDAO();
    private Team currentTeam;
    private Runnable onSuccess;

    public void setTeam(Team team) {
        this.currentTeam = team;
        if (team != null) {
            formTitle.setText("Edit Team");
            nameField.setText(team.getName());
            logoField.setText(team.getLogo());
            descField.setText(team.getDescription());
            captainField.setText(String.valueOf(team.getCaptain_id()));
            deleteBtn.setVisible(true);
        } else {
            formTitle.setText("Add Team");
            deleteBtn.setVisible(false);
        }
    }

    public void setOnSuccess(Runnable onSuccess) { this.onSuccess = onSuccess; }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty()) return;
        if (currentTeam == null) {
            Team t = new Team(nameField.getText(), logoField.getText(), descField.getText(), Integer.parseInt(captainField.getText()));
            teamDAO.add(t);
        } else {
            currentTeam.setName(nameField.getText());
            currentTeam.setLogo(logoField.getText());
            currentTeam.setDescription(descField.getText());
            currentTeam.setCaptain_id(Integer.parseInt(captainField.getText()));
            teamDAO.update(currentTeam);
        }
        if (onSuccess != null) onSuccess.run();
        ((Stage) nameField.getScene().getWindow()).close();
    }

    @FXML private void handleDelete() {
        if (currentTeam != null) {
            teamDAO.delete(currentTeam.getId());
            if (onSuccess != null) onSuccess.run();
            ((Stage) nameField.getScene().getWindow()).close();
        }
    }

    @FXML private void handleBrowse() { /* FileChooser logic if needed */ }
    @FXML private void handleCancel() { ((Stage) nameField.getScene().getWindow()).close(); }
}