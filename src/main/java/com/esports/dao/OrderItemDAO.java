package com.esports.dao;

import com.esports.db.DatabaseConnection;
import com.esports.models.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderItemDAO {

    private final Connection connection;

    public OrderItemDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean addOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_item (order_ref_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, item.getOrderRefId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("❌ Error while adding order item: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteItemsByOrderId(int orderId) {
        String sql = "DELETE FROM order_item WHERE order_ref_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("❌ Error while deleting order items: " + e.getMessage());
            return false;
        }
    }
}