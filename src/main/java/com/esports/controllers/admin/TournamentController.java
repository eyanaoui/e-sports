package com.esports.controllers.admin;

import com.esports.dao.TournamentDAO;
import com.esports.models.Tournament;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TournamentController {
    @FXML private TextField searchField;
    @FXML private TableView<Tournament> tournamentTable;
    @FXML private TableColumn<Tournament, Integer> colId;
    @FXML private TableColumn<Tournament, String> colName, colGame, colFormat, colStatus;
    @FXML private TableColumn<Tournament, LocalDateTime> colDate;

    private TournamentDAO tournamentDAO = new TournamentDAO();
    private ObservableList<Tournament> tournamentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Basic Column Mapping
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colFormat.setCellValueFactory(new PropertyValueFactory<>("format"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom Formatter for the Date Column
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        colDate.setCellValueFactory(new PropertyValueFactory<>("start_date"));
        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(formatter.format(item));
            }
        });

        tournamentTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Detect double click
                Tournament selected = tournamentTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openForm(selected); // Opens the form with data populated
                }
            }
        });

        loadTournaments();
    }

    private void loadTournaments() {
        tournamentList.setAll(tournamentDAO.getAll());
        tournamentTable.setItems(tournamentList);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) { loadTournaments(); return; }

        List<Tournament> filtered = tournamentDAO.getAll().stream()
                .filter(t -> t.getName().toLowerCase().contains(query) ||
                        t.getGame().toLowerCase().contains(query))
                .collect(Collectors.toList());
        tournamentList.setAll(filtered);
    }

    @FXML
    private void handleAdd() {
        openForm(null);
    }

    private void openForm(Tournament tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/tournament-form.fxml"));
            Parent parent = loader.load();

            TournamentFormController controller = loader.getController();
            controller.setTournament(tournament);
            controller.setOnSuccess(this::loadTournaments);

            Stage stage = new Stage();
            stage.setTitle(tournament == null ? "Add Tournament" : "Edit Tournament");
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}