package com.esports.controllers;

import com.esports.dao.GameDAO;
import com.esports.models.Game;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class GameController {

    @FXML private TextField nameField, slugField, descField, searchField;
    @FXML private CheckBox rankingCheck;
    @FXML private TableView<Game> gameTable;
    @FXML private TableColumn<Game, Integer> colId;
    @FXML private TableColumn<Game, String>  colName, colSlug, colDescription;
    @FXML private TableColumn<Game, Boolean> colRanking;

    private GameDAO gameDAO = new GameDAO();
    private ObservableList<Game> gameList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSlug.setCellValueFactory(new PropertyValueFactory<>("slug"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colRanking.setCellValueFactory(new PropertyValueFactory<>("hasRanking"));
        loadGames();

        // click on row to fill form
        gameTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                nameField.setText(selected.getName());
                slugField.setText(selected.getSlug());
                descField.setText(selected.getDescription());
                rankingCheck.setSelected(selected.isHasRanking());
            }
        });
    }

    private void loadGames() {
        gameList.setAll(gameDAO.getAll());
        gameTable.setItems(gameList);
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) return;
        Game g = new Game(
                nameField.getText().trim(),
                slugField.getText().trim(),
                descField.getText().trim(),
                null,
                rankingCheck.isSelected()
        );
        gameDAO.add(g);
        loadGames();
        handleClear();
    }

    @FXML
    private void handleUpdate() {
        Game selected = gameTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Please select a game to update."); return; }
        if (!validateInputs()) return;
        selected.setName(nameField.getText().trim());
        selected.setSlug(slugField.getText().trim());
        selected.setDescription(descField.getText().trim());
        selected.setHasRanking(rankingCheck.isSelected());
        gameDAO.update(selected);
        loadGames();
        handleClear();
    }

    @FXML
    private void handleDelete() {
        Game selected = gameTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Please select a game to delete."); return; }
        gameDAO.delete(selected.getId());
        loadGames();
        handleClear();
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        slugField.clear();
        descField.clear();
        rankingCheck.setSelected(false);
        searchField.clear();
        gameTable.getSelectionModel().clearSelection();
        loadGames();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) { loadGames(); return; }
        List<Game> filtered = gameDAO.getAll().stream()
                .filter(g -> g.getName().toLowerCase().contains(query)
                        || g.getSlug().toLowerCase().contains(query))
                .collect(Collectors.toList());
        gameList.setAll(filtered);
        gameTable.setItems(gameList);
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) { showAlert("Name is required!"); return false; }
        if (slugField.getText().trim().isEmpty())  { showAlert("Slug is required!"); return false; }
        if (nameField.getText().trim().length() < 2) { showAlert("Name must be at least 2 characters!"); return false; }
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