package com.example.hotelio.services;

import com.example.hotelio.entities.Reservation;
import com.example.hotelio.enums.StatutReservation;
import com.example.hotelio.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    private ChambreService chambreService = new ChambreService();
    private UserService userService = new UserService();

    // Créer une réservation
    public boolean creerReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (user_id, chambre_id, date_checkin, " +
                "date_checkout, nombre_personnes, prix_total, statut, commentaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reservation.getUserId());
            stmt.setInt(2, reservation.getChambreId());
            stmt.setDate(3, Date.valueOf(reservation.getDateCheckin()));
            stmt.setDate(4, Date.valueOf(reservation.getDateCheckout()));
            stmt.setInt(5, reservation.getNombrePersonnes());
            stmt.setDouble(6, reservation.getPrixTotal());
            stmt.setString(7, reservation.getStatut().name());
            stmt.setString(8, reservation.getCommentaire());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservation.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur création réservation: " + e.getMessage());
        }
        return false;
    }

    // Obtenir toutes les réservations
    public List<Reservation> obtenirToutesLesReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, u.nom, u.prenom, u.email, c.numero, c.type " +
                "FROM reservations r " +
                "JOIN users u ON r.user_id = u.id " +
                "JOIN chambres c ON r.chambre_id = c.id " +
                "ORDER BY r.date_reservation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération réservations: " + e.getMessage());
        }
        return reservations;
    }

    // Obtenir réservations par utilisateur
    public List<Reservation> obtenirReservationsParUtilisateur(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, c.numero, c.type, c.prix_par_nuit " +
                "FROM reservations r " +
                "JOIN chambres c ON r.chambre_id = c.id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.date_checkin DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération réservations utilisateur: " + e.getMessage());
        }
        return reservations;
    }

    // Modifier statut réservation
    public boolean modifierStatutReservation(int reservationId, StatutReservation statut) {
        String sql = "UPDATE reservations SET statut = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut.name());
            stmt.setInt(2, reservationId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification statut réservation: " + e.getMessage());
        }
        return false;
    }

    // Annuler une réservation
    public boolean annulerReservation(int reservationId) {
        return modifierStatutReservation(reservationId, StatutReservation.ANNULEE);
    }

    // Confirmer une réservation
    public boolean confirmerReservation(int reservationId) {
        return modifierStatutReservation(reservationId, StatutReservation.CONFIRMEE);
    }

    // Mapper ResultSet vers Reservation
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        reservation.setUserId(rs.getInt("user_id"));
        reservation.setChambreId(rs.getInt("chambre_id"));
        reservation.setDateCheckin(rs.getDate("date_checkin").toLocalDate());
        reservation.setDateCheckout(rs.getDate("date_checkout").toLocalDate());
        reservation.setNombrePersonnes(rs.getInt("nombre_personnes"));
        reservation.setPrixTotal(rs.getDouble("prix_total"));
        reservation.setStatut(StatutReservation.valueOf(rs.getString("statut")));
        reservation.setCommentaire(rs.getString("commentaire"));
        reservation.setDateReservation(rs.getTimestamp("date_reservation").toLocalDateTime());
        return reservation;
    }
}
