package com.esports.controllers.user;

import com.esports.dao.OrderDAO;
import com.esports.dao.OrderItemDAO;
import com.esports.dao.ProductDAO;
import com.esports.models.CartItem;
import com.esports.models.Order;
import com.esports.models.OrderItem;
import com.esports.models.Product;
import com.esports.utils.OrderValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopController {

    @FXML
    private FlowPane productsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<CartItem> cartListView;

    @FXML
    private Label cartTotalLabel;

    @FXML
    private TextField firstNameField;

    @FXML
    private Label firstNameError;

    @FXML
    private TextField lastNameField;

    @FXML
    private Label lastNameError;

    @FXML
    private TextField emailField;

    @FXML
    private Label emailError;

    @FXML
    private TextField phoneField;

    @FXML
    private Label phoneError;

    @FXML
    private ComboBox<String> paymentMethodBox;

    @FXML
    private Label paymentError;

    @FXML
    private Label orderMessageLabel;

    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    private List<Product> allProducts = new ArrayList<>();
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        paymentMethodBox.getItems().addAll("Cash", "Card", "PayPal");
        cartListView.setItems(cartItems);
        loadProducts();
        refreshCartView();
        clearErrors();
        orderMessageLabel.setText("");
    }

    private void loadProducts() {
        allProducts = productDAO.getAllProducts();
        renderProducts(allProducts);
    }

    private void renderProducts(List<Product> products) {
        productsContainer.getChildren().clear();

        for (Product product : products) {
            VBox card = new VBox(10);
            card.setPrefWidth(220);
            card.setPadding(new Insets(12));
            card.setStyle(
                    "-fx-background-color: #161a30;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #2b2f55;" +
                            "-fx-border-radius: 12;"
            );

            ImageView imageView = new ImageView();
            imageView.setFitWidth(190);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(false);

            try {
                Image image = new Image(product.getImage(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                // ignore
            }

            Label nameLabel = new Label(product.getName());
            nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");

            Label categoryLabel = new Label(product.getCategory());
            categoryLabel.setStyle("-fx-text-fill: #b8b8d1;");

            Label priceLabel = new Label("Price: " + product.getPrice());
            priceLabel.setStyle("-fx-text-fill: white;");

            Label stockLabel = new Label("Stock: " + product.getStock());
            stockLabel.setStyle("-fx-text-fill: white;");

            Label descLabel = new Label(product.getDescription());
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-text-fill: #d8d8e8;");

            Spinner<Integer> qtySpinner = new Spinner<>(1, Math.max(1, product.getStock()), 1);
            qtySpinner.setEditable(true);
            qtySpinner.setPrefWidth(90);

            Button addToCartBtn = new Button("Add to Cart");
            addToCartBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
            addToCartBtn.setOnAction(e -> handleAddToCart(product, qtySpinner.getValue()));

            HBox bottomRow = new HBox(10, qtySpinner, addToCartBtn);

            card.getChildren().addAll(
                    imageView,
                    nameLabel,
                    categoryLabel,
                    priceLabel,
                    stockLabel,
                    descLabel,
                    bottomRow
            );

            productsContainer.getChildren().add(card);
        }
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            renderProducts(allProducts);
            return;
        }

        List<Product> filtered = allProducts.stream()
                .filter(p ->
                        p.getName().toLowerCase().contains(keyword)
                                || p.getCategory().toLowerCase().contains(keyword)
                                || p.getDescription().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        renderProducts(filtered);
    }

    @FXML
    public void handleRefresh() {
        searchField.clear();
        loadProducts();
    }

    private void handleAddToCart(Product product, int quantity) {
        if (quantity <= 0) {
            return;
        }

        CartItem existing = null;
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                existing = item;
                break;
            }
        }

        int cartQuantity = quantity;
        if (existing != null) {
            cartQuantity += existing.getQuantity();
        }

        if (cartQuantity > product.getStock()) {
            orderMessageLabel.setStyle("-fx-text-fill: red;");
            orderMessageLabel.setText("Cannot add more than available stock for " + product.getName());
            return;
        }

        if (existing != null) {
            existing.increaseQuantity(quantity);
        } else {
            cartItems.add(new CartItem(product, quantity));
        }

        refreshCartView();
        orderMessageLabel.setStyle("-fx-text-fill: green;");
        orderMessageLabel.setText(product.getName() + " added to cart.");
    }

    @FXML
    public void handleRemoveSelected() {
        CartItem selected = cartListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            refreshCartView();
        }
    }

    @FXML
    public void handleClearCart() {
        cartItems.clear();
        refreshCartView();
    }

    private void refreshCartView() {
        cartListView.refresh();
        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        cartTotalLabel.setText("Cart Total: " + total);
    }

    @FXML
    public void handlePlaceOrder() {
        clearErrors();
        orderMessageLabel.setText("");

        if (cartItems.isEmpty()) {
            orderMessageLabel.setStyle("-fx-text-fill: red;");
            orderMessageLabel.setText("Your cart is empty.");
            return;
        }

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String paymentMethod = paymentMethodBox.getValue();

        String firstNameMsg = OrderValidator.validateFirstName(firstName);
        String lastNameMsg = OrderValidator.validateLastName(lastName);
        String emailMsg = OrderValidator.validateEmail(email);
        String phoneMsg = OrderValidator.validatePhone(phone);
        String paymentMsg = OrderValidator.validatePaymentMethod(paymentMethod);

        boolean isValid = true;

        if (!firstNameMsg.isEmpty()) {
            firstNameError.setText(firstNameMsg);
            isValid = false;
        }
        if (!lastNameMsg.isEmpty()) {
            lastNameError.setText(lastNameMsg);
            isValid = false;
        }
        if (!emailMsg.isEmpty()) {
            emailError.setText(emailMsg);
            isValid = false;
        }
        if (!phoneMsg.isEmpty()) {
            phoneError.setText(phoneMsg);
            isValid = false;
        }
        if (!paymentMsg.isEmpty()) {
            paymentError.setText(paymentMsg);
            isValid = false;
        }

        for (CartItem item : cartItems) {
            Product latest = productDAO.getProductById(item.getProduct().getId());
            if (latest == null || latest.getStock() < item.getQuantity()) {
                orderMessageLabel.setStyle("-fx-text-fill: red;");
                orderMessageLabel.setText("Not enough stock for: " + item.getProduct().getName());
                isValid = false;
                break;
            }
        }

        if (!isValid) {
            return;
        }

        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        String reference = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order(
                LocalDateTime.now(),
                email,
                firstName,
                lastName,
                phone,
                paymentMethod,
                "PENDING",
                reference,
                "NEW",
                total
        );

        int orderId = orderDAO.addOrder(order);

        if (orderId == -1) {
            orderMessageLabel.setStyle("-fx-text-fill: red;");
            orderMessageLabel.setText("Order creation failed.");
            return;
        }

        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem(
                    orderId,
                    item.getProduct().getId(),
                    item.getQuantity(),
                    item.getProduct().getPrice()
            );

            boolean itemSaved = orderItemDAO.addOrderItem(orderItem);
            boolean stockUpdated = productDAO.decreaseStock(item.getProduct().getId(), item.getQuantity());

            if (!itemSaved || !stockUpdated) {
                orderMessageLabel.setStyle("-fx-text-fill: red;");
                orderMessageLabel.setText("Order item save failed for " + item.getProduct().getName());
                return;
            }
        }

        orderMessageLabel.setStyle("-fx-text-fill: green;");
        orderMessageLabel.setText("Order placed successfully. Ref: " + reference);

        cartItems.clear();
        refreshCartView();
        clearForm();
        loadProducts();
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        paymentMethodBox.setValue(null);
        clearErrors();
    }

    private void clearErrors() {
        firstNameError.setText("");
        lastNameError.setText("");
        emailError.setText("");
        phoneError.setText("");
        paymentError.setText("");
    }
}