package com.example.hotelio.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/hotelio_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Modifier selon votre configuration

    private static Connection connection = null;

    // Connexion singleton
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Connexion à la base de données établie");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver MySQL non trouvé: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    // Fermer la connexion
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Connexion fermée");
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la fermeture: " + e.getMessage());
        }
    }

    // Test de connexion
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}