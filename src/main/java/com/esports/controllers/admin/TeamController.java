package com.esports.controllers.admin;

import com.esports.dao.TeamDAO;
import com.esports.models.Team;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TeamController {
    @FXML private TextField searchField;
    @FXML private TableView<Team> teamTable;
    @FXML private TableColumn<Team, Integer> colId;
    @FXML private TableColumn<Team, String> colName, colLogo, colDescription;
    @FXML private TableColumn<Team, Integer> colCaptain;
    // Added these two to match your FXML and display the dates
    @FXML private TableColumn<Team, LocalDateTime> colCreatedAt, colUpdatedAt;

    private TeamDAO teamDAO = new TeamDAO();
    private ObservableList<Team> teamList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLogo.setCellValueFactory(new PropertyValueFactory<>("logo"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCaptain.setCellValueFactory(new PropertyValueFactory<>("captain_id"));

        // Wiring the date columns
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        colUpdatedAt.setCellValueFactory(new PropertyValueFactory<>("updated_at"));

        loadTeams();

        teamTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Team selected = teamTable.getSelectionModel().getSelectedItem();
                if (selected != null) openForm(selected);
            }
        });
    }

    private void loadTeams() {
        teamList.setAll(teamDAO.getAll());
        teamTable.setItems(teamList);
    }

    @FXML private void handleAdd() { openForm(null); }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) { loadTeams(); return; }
        List<Team> filtered = teamDAO.getAll().stream()
                .filter(t -> (t.getName() != null && t.getName().toLowerCase().contains(query)) ||
                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        teamList.setAll(filtered);
    }

    private void openForm(Team team) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/team-form.fxml"));
            Parent parent = loader.load();
            TeamFormController controller = loader.getController();
            controller.setTeam(team);
            controller.setOnSuccess(this::loadTeams);
            Stage stage = new Stage();
            stage.setTitle(team == null ? "Add Team" : "Edit Team");
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Error opening form: " + e.getMessage());
            e.printStackTrace();
        }
    }
}