package com.example.hotelio.services;

import com.example.hotelio.entities.*;
import com.example.hotelio.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    // Créer un utilisateur (Client ou Admin)
    public boolean creerUtilisateur(User user) {
        String typeUtilisateur = user.getTypeUtilisateur();

        String sql = "INSERT INTO users (nom, prenom, email, telephone, mot_de_passe, role) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getTelephone());
            stmt.setString(5, user.getMotDePasse());
            stmt.setString(6, typeUtilisateur); // CLIENT ou ADMIN

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

    // Authentification - retourne le bon type d'utilisateur
    public User authentifier(String email, String motDePasse) {
        String sql = "SELECT * FROM users WHERE email = ? AND mot_de_passe = ? AND actif = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, motDePasse);

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

    // Obtenir seulement les clients
    public List<Client> obtenirTousLesClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'CLIENT' ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                if (user instanceof Client) {
                    clients.add((Client) user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération clients: " + e.getMessage());
        }
        return clients;
    }

    // Obtenir seulement les admins
    public List<Admin> obtenirTousLesAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'ADMIN' ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                if (user instanceof Admin) {
                    admins.add((Admin) user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération admins: " + e.getMessage());
        }
        return admins;
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
            stmt.setString(5, user.getTypeUtilisateur());
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

    // Mapper ResultSet vers User (Client ou Admin)
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;

        if ("ADMIN".equals(role)) {
            user = new Admin();
        } else {
            user = new Client();
        }

        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setTelephone(rs.getString("telephone"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        user.setActif(rs.getBoolean("actif"));
        user.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());

        return user;
    }
}