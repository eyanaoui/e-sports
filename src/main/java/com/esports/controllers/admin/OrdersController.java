package com.esports.controllers.admin;

import com.esports.dao.OrderDAO;
import com.esports.dao.OrderItemDAO;
import com.esports.models.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class OrdersController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Order> orderTable;

    @FXML
    private TableColumn<Order, Integer> idColumn;

    @FXML
    private TableColumn<Order, String> referenceColumn;

    @FXML
    private TableColumn<Order, String> firstNameColumn;

    @FXML
    private TableColumn<Order, String> lastNameColumn;

    @FXML
    private TableColumn<Order, String> emailColumn;

    @FXML
    private TableColumn<Order, String> phoneColumn;

    @FXML
    private TableColumn<Order, String> paymentMethodColumn;

    @FXML
    private TableColumn<Order, String> paymentStatusColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private TableColumn<Order, Double> totalColumn;

    @FXML
    private TableColumn<Order, String> createdAtColumn;

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        referenceColumn.setCellValueFactory(new PropertyValueFactory<>("reference"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerFirstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerLastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        createdAtColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt() != null
                                ? cellData.getValue().getCreatedAt().toString()
                                : ""
                )
        );

        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders = orderDAO.getAllOrders();
        orderList = FXCollections.observableArrayList(orders);
        orderTable.setItems(orderList);
    }

    @FXML
    public void handleRefresh() {
        searchField.clear();
        loadOrders();
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            orderTable.setItems(orderList);
            return;
        }

        List<Order> filtered = orderList.stream()
                .filter(order ->
                        (order.getReference() != null && order.getReference().toLowerCase().contains(keyword)) ||
                                (order.getCustomerFirstName() != null && order.getCustomerFirstName().toLowerCase().contains(keyword)) ||
                                (order.getCustomerLastName() != null && order.getCustomerLastName().toLowerCase().contains(keyword)) ||
                                (order.getCustomerEmail() != null && order.getCustomerEmail().toLowerCase().contains(keyword)) ||
                                (order.getStatus() != null && order.getStatus().toLowerCase().contains(keyword)) ||
                                (order.getPaymentStatus() != null && order.getPaymentStatus().toLowerCase().contains(keyword))
                )
                .collect(Collectors.toList());

        orderTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void handleDeleteSelected() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an order.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Order");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this order?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean itemsDeleted = orderItemDAO.deleteItemsByOrderId(selectedOrder.getId());
            boolean orderDeleted = false;

            if (itemsDeleted) {
                orderDeleted = orderDAO.deleteOrder(selectedOrder.getId());
            }

            if (orderDeleted) {
                loadOrders();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Order was not deleted.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}