package com.esports.dao;

import com.esports.db.DatabaseConnection;
import com.esports.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private final Connection connection;

    public ProductDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO product (category, description, image, is_active, name, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getCategory());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getImage());
            ps.setBoolean(4, product.isActive());
            ps.setString(5, product.getName());
            ps.setDouble(6, product.getPrice());
            ps.setInt(7, product.getStock());
            ps.executeUpdate();
            System.out.println("✅ Product added successfully.");
        } catch (SQLException e) {
            System.out.println("❌ Error while adding product: " + e.getMessage());
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT p.*, COUNT(oi.id) AS orders_count
                FROM product p
                LEFT JOIN order_item oi ON oi.product_id = p.id
                WHERE p.is_active = true
                GROUP BY p.id, p.category, p.description, p.image, p.is_active, p.name, p.price, p.stock
                ORDER BY p.id DESC
                """;

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setCategory(rs.getString("category"));
                product.setDescription(rs.getString("description"));
                product.setImage(rs.getString("image"));
                product.setActive(rs.getBoolean("is_active"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setOrdersCount(rs.getInt("orders_count"));

                products.add(product);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error while displaying products: " + e.getMessage());
        }

        return products;
    }

    public boolean softDeleteProduct(int id) {
        String sql = "UPDATE product SET is_active = false WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Error while deactivating product: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStock(int productId, int newStock) {
        String sql = "UPDATE product SET stock=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Error while updating stock: " + e.getMessage());
            return false;
        }
    }

    public boolean decreaseStock(int productId, int quantity) {
        String sql = "UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Error while decreasing stock: " + e.getMessage());
            return false;
        }
    }

    public void updateProduct(Product product) {
        String sql = "UPDATE product SET category=?, description=?, image=?, is_active=?, name=?, price=?, stock=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getCategory());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getImage());
            ps.setBoolean(4, product.isActive());
            ps.setString(5, product.getName());
            ps.setDouble(6, product.getPrice());
            ps.setInt(7, product.getStock());
            ps.setInt(8, product.getId());
            ps.executeUpdate();
            System.out.println("✅ Product updated successfully.");
        } catch (SQLException e) {
            System.out.println("❌ Error while updating product: " + e.getMessage());
        }
    }

    public Product getProductById(int id) {
        String sql = "SELECT * FROM product WHERE id=?";
        Product product = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                product = new Product();
                product.setId(rs.getInt("id"));
                product.setCategory(rs.getString("category"));
                product.setDescription(rs.getString("description"));
                product.setImage(rs.getString("image"));
                product.setActive(rs.getBoolean("is_active"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error while searching product: " + e.getMessage());
        }

        return product;
    }
}