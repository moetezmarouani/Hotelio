package com.example.hotelio.services;

import com.example.hotelio.entities.Chambre;
import com.example.hotelio.enums.StatutChambre;
import com.example.hotelio.enums.TypeChambre;
import com.example.hotelio.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChambreService {

    // Ajouter une chambre
    public boolean ajouterChambre(Chambre chambre) {
        String sql = "INSERT INTO chambres (numero, type, prix_par_nuit, capacite, " +
                "description, statut, etage) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, chambre.getNumero());
            stmt.setString(2, chambre.getType().name());
            stmt.setDouble(3, chambre.getPrixParNuit());
            stmt.setInt(4, chambre.getCapacite());
            stmt.setString(5, chambre.getDescription());
            stmt.setString(6, chambre.getStatut().name());
            stmt.setInt(7, chambre.getEtage());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        chambre.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout chambre: " + e.getMessage());
        }
        return false;
    }

    // Obtenir toutes les chambres
    public List<Chambre> obtenirToutesLesChambres() {
        List<Chambre> chambres = new ArrayList<>();
        String sql = "SELECT * FROM chambres ORDER BY numero";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                chambres.add(mapResultSetToChambre(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération chambres: " + e.getMessage());
        }
        return chambres;
    }

    // Obtenir chambres disponibles
    public List<Chambre> obtenirChambresDisponibles(LocalDate dateDebut, LocalDate dateFin) {
        List<Chambre> chambres = new ArrayList<>();
        String sql = "SELECT c.* FROM chambres c " +
                "WHERE c.statut = 'DISPONIBLE' " +
                "AND c.id NOT IN (" +
                "  SELECT chambre_id FROM reservations " +
                "  WHERE statut IN ('EN_ATTENTE', 'CONFIRMEE') " +
                "  AND ((date_checkin <= ? AND date_checkout >= ?) " +
                "       OR (date_checkin <= ? AND date_checkout >= ?) " +
                "       OR (date_checkin >= ? AND date_checkout <= ?))" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(dateDebut));
            stmt.setDate(2, Date.valueOf(dateDebut));
            stmt.setDate(3, Date.valueOf(dateFin));
            stmt.setDate(4, Date.valueOf(dateFin));
            stmt.setDate(5, Date.valueOf(dateDebut));
            stmt.setDate(6, Date.valueOf(dateFin));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chambres.add(mapResultSetToChambre(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche disponibilité: " + e.getMessage());
        }
        return chambres;
    }

    // Modifier une chambre
    public boolean modifierChambre(Chambre chambre) {
        String sql = "UPDATE chambres SET numero = ?, type = ?, prix_par_nuit = ?, " +
                "capacite = ?, description = ?, statut = ?, etage = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, chambre.getNumero());
            stmt.setString(2, chambre.getType().name());
            stmt.setDouble(3, chambre.getPrixParNuit());
            stmt.setInt(4, chambre.getCapacite());
            stmt.setString(5, chambre.getDescription());
            stmt.setString(6, chambre.getStatut().name());
            stmt.setInt(7, chambre.getEtage());
            stmt.setInt(8, chambre.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification chambre: " + e.getMessage());
        }
        return false;
    }

    // Modifier le statut d'une chambre
    public boolean modifierStatutChambre(int chambreId, StatutChambre statut) {
        String sql = "UPDATE chambres SET statut = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut.name());
            stmt.setInt(2, chambreId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification statut: " + e.getMessage());
        }
        return false;
    }

    // Supprimer une chambre
    public boolean supprimerChambre(int id) {
        String sql = "DELETE FROM chambres WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression chambre: " + e.getMessage());
        }
        return false;
    }

    // Mapper ResultSet vers Chambre
    private Chambre mapResultSetToChambre(ResultSet rs) throws SQLException {
        Chambre chambre = new Chambre();
        chambre.setId(rs.getInt("id"));
        chambre.setNumero(rs.getString("numero"));
        chambre.setType(TypeChambre.valueOf(rs.getString("type")));
        chambre.setPrixParNuit(rs.getDouble("prix_par_nuit"));
        chambre.setCapacite(rs.getInt("capacite"));
        chambre.setDescription(rs.getString("description"));
        chambre.setStatut(StatutChambre.valueOf(rs.getString("statut")));
        chambre.setEtage(rs.getInt("etage"));
        chambre.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        return chambre;
    }
    // Obtenir une chambre par son ID
    public Chambre obtenirChambreParId(int id) {
        String sql = "SELECT * FROM chambres WHERE id = ?";
        Chambre chambre = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    chambre = mapResultSetToChambre(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération chambre par ID: " + e.getMessage());
        }

        return chambre;
    }

}
