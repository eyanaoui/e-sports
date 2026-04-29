package com.esports.controllers.admin;

import com.esports.dao.TournamentDAO;
import com.esports.models.Tournament;
import com.esports.services.AIService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.stream.Collectors;

public class TournamentController {
    @FXML private TableView<Tournament> tournamentTable;
    @FXML private TableColumn<Tournament, Integer> colId;
    @FXML private TableColumn<Tournament, String> colName, colGame, colStatus, colStartDate;
    @FXML private TableColumn<Tournament, Void> colActions;
    @FXML private TextField searchField;

    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private final AIService aiService = new AIService();
    private final ObservableList<Tournament> tournamentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        setupActions();
        loadTournaments();
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button edit = new Button("✎");
            private final Button del = new Button("🗑");
            private final HBox container = new HBox(10, edit, del);
            {
                edit.setOnAction(e -> openForm(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    public void loadTournaments() {
        tournamentList.setAll(tournamentDAO.getAll());
        tournamentTable.setItems(tournamentList);
    }

    @FXML private void handleAdd() { openForm(null); }

    @FXML private void handleSearch() {
        String q = searchField.getText().toLowerCase();
        ObservableList<Tournament> filtered = tournamentDAO.getAll().stream()
                .filter(t -> t.getName().toLowerCase().contains(q) || t.getGame().toLowerCase().contains(q))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tournamentTable.setItems(filtered);
    }

    @FXML private void handleAIPrediction() {
        Tournament t = tournamentTable.getSelectionModel().getSelectedItem();
        if (t != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("AI Analysis for " + t.getName());
            alert.setContentText(aiService.getTournamentPrediction(t.getId()));
            alert.show();
        }
    }

    private void openForm(Tournament t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/tournament-form.fxml"));
            Parent root = loader.load();
            TournamentFormController ctrl = loader.getController();
            ctrl.setTournament(t);
            ctrl.setOnSuccess(this::loadTournaments);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleDelete(Tournament t) {
        tournamentDAO.delete(t.getId());
        loadTournaments();
    }
}