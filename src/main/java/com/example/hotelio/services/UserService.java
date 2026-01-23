package com.example.hotelio.services;

import com.example.hotelio.entities.User;
import com.example.hotelio.enums.Role;
import com.example.hotelio.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    // Créer un utilisateur
    public boolean creerUtilisateur(User user) {
        String sql = "INSERT INTO users (nom, prenom, email, telephone, mot_de_passe, role) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getTelephone());
            stmt.setString(5, user.getMotDePasse()); // À hacher en production!
            stmt.setString(6, user.getRole().name());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur création utilisateur: " + e.getMessage());
        }
        return false;
    }

    // Authentification
    public User authentifier(String email, String motDePasse) {
        String sql = "SELECT * FROM users WHERE email = ? AND mot_de_passe = ? AND actif = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, motDePasse); // À hacher en production!

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur authentification: " + e.getMessage());
        }
        return null;
    }

    // Obtenir tous les utilisateurs
    public List<User> obtenirTousLesUtilisateurs() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération utilisateurs: " + e.getMessage());
        }
        return users;
    }

    // Obtenir un utilisateur par ID
    public User obtenirUtilisateurParId(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération utilisateur: " + e.getMessage());
        }
        return null;
    }

    // Modifier un utilisateur
    public boolean modifierUtilisateur(User user) {
        String sql = "UPDATE users SET nom = ?, prenom = ?, email = ?, " +
                "telephone = ?, role = ?, actif = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getTelephone());
            stmt.setString(5, user.getRole().name());
            stmt.setBoolean(6, user.isActif());
            stmt.setInt(7, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification utilisateur: " + e.getMessage());
        }
        return false;
    }

    // Supprimer un utilisateur
    public boolean supprimerUtilisateur(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression utilisateur: " + e.getMessage());
        }
        return false;
    }

    // Mapper ResultSet vers User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setTelephone(rs.getString("telephone"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setActif(rs.getBoolean("actif"));
        user.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        return user;
    }
}
