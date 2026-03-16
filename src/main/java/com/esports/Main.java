package com.esports;

import com.esports.db.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.getInstance();
    }
}