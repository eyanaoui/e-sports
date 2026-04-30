package com.esports.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL      = "jdbc:mysql://localhost:3306/esports_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[SUCCESS] Connected to database!");
        } catch (SQLException e) {
            System.out.println("[ERROR] Connection failed: " + e.getMessage());
            throw new RuntimeException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        // Validate connection is not null
        if (connection == null) {
            throw new RuntimeException("Database connection is null. Connection was not established.");
        }
        
        // Validate connection is still valid
        try {
            if (connection.isClosed() || !connection.isValid(2)) {
                // Attempt to reconnect
                System.out.println("[WARNING] Connection invalid, attempting to reconnect...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[SUCCESS] Reconnected to database!");
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Connection validation/reconnection failed: " + e.getMessage());
            throw new RuntimeException("Database connection is invalid and reconnection failed: " + e.getMessage(), e);
        }
        
        return connection;
    }
}