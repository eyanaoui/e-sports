package com.esports.controllers.admin;

import com.esports.dao.ProductDAO;
import com.esports.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductsController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> idColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, String> descriptionColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private TableColumn<Product, Integer> ordersCountColumn;

    @FXML
    private TableColumn<Product, Boolean> activeColumn;

    @FXML
    private TableColumn<Product, String> imageColumn;

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label totalStockLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label averagePriceLabel;

    @FXML
    private BarChart<String, Number> ordersBarChart;

    @FXML
    private PieChart categoryPieChart;

    private final ProductDAO productDAO = new ProductDAO();
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        ordersCountColumn.setCellValueFactory(new PropertyValueFactory<>("ordersCount"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        loadProducts();
    }

    private void loadProducts() {
        List<Product> products = productDAO.getAllProducts();
        productList = FXCollections.observableArrayList(products);
        productTable.setItems(productList);
        updateStatsAndCharts(productList);
    }

    private void updateStatsAndCharts(List<Product> products) {
        int totalProducts = products.size();
        int totalStock = products.stream().mapToInt(Product::getStock).sum();
        int totalOrders = products.stream().mapToInt(Product::getOrdersCount).sum();
        double avgPrice = products.stream().mapToDouble(Product::getPrice).average().orElse(0.0);

        totalProductsLabel.setText(String.valueOf(totalProducts));
        totalStockLabel.setText(String.valueOf(totalStock));
        totalOrdersLabel.setText(String.valueOf(totalOrders));
        averagePriceLabel.setText(String.format("%.2f", avgPrice));

        updateBarChart(products);
        updatePieChart(products);
    }

    private void updateBarChart(List<Product> products) {
        ordersBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Orders");

        products.stream()
                .sorted(Comparator.comparingInt(Product::getOrdersCount).reversed())
                .limit(8)
                .forEach(product ->
                        series.getData().add(new XYChart.Data<>(product.getName(), product.getOrdersCount()))
                );

        ordersBarChart.getData().add(series);
    }

    private void updatePieChart(List<Product> products) {
        categoryPieChart.getData().clear();

        Map<String, Long> categoryCount = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Long> entry : categoryCount.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        categoryPieChart.setData(pieData);
    }

    @FXML
    public void handleRefresh() {
        searchField.clear();
        loadProducts();
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            productTable.setItems(productList);
            updateStatsAndCharts(productList);
            return;
        }

        List<Product> filtered = productList.stream()
                .filter(product ->
                        product.getName().toLowerCase().contains(keyword)
                                || product.getCategory().toLowerCase().contains(keyword)
                                || product.getDescription().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        ObservableList<Product> filteredList = FXCollections.observableArrayList(filtered);
        productTable.setItems(filteredList);
        updateStatsAndCharts(filteredList);
    }

    @FXML
    public void handleSortByName() {
        sortAndDisplay(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
    }

    @FXML
    public void handleSortByPriceAsc() {
        sortAndDisplay(Comparator.comparingDouble(Product::getPrice));
    }

    @FXML
    public void handleSortByPriceDesc() {
        sortAndDisplay(Comparator.comparingDouble(Product::getPrice).reversed());
    }

    @FXML
    public void handleSortByStockAsc() {
        sortAndDisplay(Comparator.comparingInt(Product::getStock));
    }

    @FXML
    public void handleSortByStockDesc() {
        sortAndDisplay(Comparator.comparingInt(Product::getStock).reversed());
    }

    @FXML
    public void handleSortByOrders() {
        sortAndDisplay(Comparator.comparingInt(Product::getOrdersCount).reversed());
    }

    private void sortAndDisplay(Comparator<Product> comparator) {
        List<Product> sorted = productTable.getItems().stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        ObservableList<Product> sortedList = FXCollections.observableArrayList(sorted);
        productTable.setItems(sortedList);
        updateStatsAndCharts(sortedList);
    }

    @FXML
    public void handleDeleteSelected() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Deactivate Product");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to deactivate this product?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean deleted = productDAO.softDeleteProduct(selectedProduct.getId());

            if (deleted) {
                loadProducts();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product deactivated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Product was not deactivated.");
            }
        }
    }

    @FXML
    public void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/product-form.fxml"));
            Parent formView = loader.load();

            AnchorPane contentArea = (AnchorPane) productTable.getScene().lookup("#contentArea");

            if (contentArea != null) {
                AnchorPane.setTopAnchor(formView, 0.0);
                AnchorPane.setBottomAnchor(formView, 0.0);
                AnchorPane.setLeftAnchor(formView, 0.0);
                AnchorPane.setRightAnchor(formView, 0.0);
                contentArea.getChildren().setAll(formView);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditSelected() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product to modify.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/product-form.fxml"));
            Parent formView = loader.load();

            ProductFormController controller = loader.getController();
            controller.setProduct(selectedProduct);

            AnchorPane contentArea = (AnchorPane) productTable.getScene().lookup("#contentArea");

            if (contentArea != null) {
                AnchorPane.setTopAnchor(formView, 0.0);
                AnchorPane.setBottomAnchor(formView, 0.0);
                AnchorPane.setLeftAnchor(formView, 0.0);
                AnchorPane.setRightAnchor(formView, 0.0);
                contentArea.getChildren().setAll(formView);
            }

        } catch (IOException e) {
            e.printStackTrace();
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