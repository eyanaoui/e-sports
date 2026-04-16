package com.esports.controllers.admin;

import com.esports.dao.MessageDao;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminForumController {

    @FXML private TextField filterField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Label totalMessagesLabel;
    @FXML private TableView<Row> messageTable;
    @FXML private TableColumn<Row, Integer> colId, colSujetId, colLength;
    @FXML private TableColumn<Row, String> colSujetTitre, colContenu;
    @FXML private PieChart sujetPieChart;

    private final MessageDao messageDao = new MessageDao();
    private final ObservableList<Row> viewItems = FXCollections.observableArrayList();
    private final List<Row> allItems = new ArrayList<>();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSujetId.setCellValueFactory(new PropertyValueFactory<>("sujetId"));
        colSujetTitre.setCellValueFactory(new PropertyValueFactory<>("sujetTitre"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colLength.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getContenu() == null ? 0 : c.getValue().getContenu().length()).asObject());

        messageTable.setItems(viewItems);
        sortCombo.getItems().setAll(
                "Newest first",
                "Oldest first",
                "Subject A-Z",
                "Subject Z-A",
                "Longest message"
        );
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.valueProperty().addListener((o, a, b) -> applyFilterAndSort());

        filterField.textProperty().addListener((o, a, b) -> applyFilterAndSort());
        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        allItems.clear();
        for (MessageDao.AdminForumMessageRow r : messageDao.getAllForAdmin()) {
            allItems.add(new Row(r.id, r.sujetId, safe(r.sujetTitre), safe(r.contenu)));
        }
        applyFilterAndSort();
        rebuildPieChart();
    }

    @FXML
    private void handleDeleteSelected() {
        Row selected = messageTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Select a message first.").showAndWait();
            return;
        }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Delete message");
        c.setHeaderText(null);
        c.setContentText("Delete selected message?");
        c.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                messageDao.delete(selected.getId());
                handleRefresh();
            }
        });
    }

    private void applyFilterAndSort() {
        String query = filterField.getText() == null ? "" : filterField.getText().trim().toLowerCase(Locale.ROOT);
        List<Row> filtered = allItems.stream()
                .filter(r -> query.isEmpty()
                        || r.getContenu().toLowerCase(Locale.ROOT).contains(query)
                        || r.getSujetTitre().toLowerCase(Locale.ROOT).contains(query)
                        || String.valueOf(r.getSujetId()).contains(query))
                .collect(Collectors.toList());

        String sort = sortCombo.getValue();
        Comparator<Row> comparator = Comparator.comparingInt(Row::getId).reversed();
        if ("Oldest first".equals(sort)) comparator = Comparator.comparingInt(Row::getId);
        if ("Subject A-Z".equals(sort)) comparator = Comparator.comparing(Row::getSujetTitre, String.CASE_INSENSITIVE_ORDER);
        if ("Subject Z-A".equals(sort)) comparator = Comparator.comparing(Row::getSujetTitre, String.CASE_INSENSITIVE_ORDER).reversed();
        if ("Longest message".equals(sort)) comparator = Comparator.comparingInt((Row r) -> r.getContenu().length()).reversed();
        filtered.sort(comparator);

        viewItems.setAll(filtered);
        totalMessagesLabel.setText(String.valueOf(filtered.size()));
    }

    private void rebuildPieChart() {
        sujetPieChart.getData().clear();
        Map<String, Integer> bySujet = messageDao.countBySujetForAdmin();
        int limit = 8;
        int i = 0;
        for (Map.Entry<String, Integer> e : bySujet.entrySet()) {
            if (i++ >= limit) break;
            sujetPieChart.getData().add(new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()));
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    public static class Row {
        private final int id;
        private final int sujetId;
        private final String sujetTitre;
        private final String contenu;

        public Row(int id, int sujetId, String sujetTitre, String contenu) {
            this.id = id;
            this.sujetId = sujetId;
            this.sujetTitre = sujetTitre;
            this.contenu = contenu;
        }

        public int getId() { return id; }
        public int getSujetId() { return sujetId; }
        public String getSujetTitre() { return sujetTitre; }
        public String getContenu() { return contenu; }
    }
}
