package com.example.hotelio.services;

import com.example.hotelio.entities.Evaluation;
import com.example.hotelio.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EvaluationService {

    // Ajouter une évaluation
    public boolean ajouterEvaluation(Evaluation evaluation) {
        String sql = "INSERT INTO evaluations (reservation_id, chambre_id, user_id, " +
                "note, commentaire) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, evaluation.getReservationId());
            stmt.setInt(2, evaluation.getChambreId());
            stmt.setInt(3, evaluation.getUserId());
            stmt.setInt(4, evaluation.getNote());
            stmt.setString(5, evaluation.getCommentaire());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajout évaluation: " + e.getMessage());
        }
        return false;
    }

    // Obtenir évaluations d'une chambre
    public List<Evaluation> obtenirEvaluationsParChambre(int chambreId) {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT * FROM evaluations WHERE chambre_id = ? ORDER BY date_evaluation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chambreId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evaluations.add(mapResultSetToEvaluation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération évaluations: " + e.getMessage());
        }
        return evaluations;
    }

    // Calculer note moyenne d'une chambre
    public double calculerNoteMoyenne(int chambreId) {
        String sql = "SELECT AVG(note) as moyenne FROM evaluations WHERE chambre_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chambreId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur calcul note moyenne: " + e.getMessage());
        }
        return 0.0;
    }

    // Mapper ResultSet vers Evaluation
    private Evaluation mapResultSetToEvaluation(ResultSet rs) throws SQLException {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(rs.getInt("id"));
        evaluation.setReservationId(rs.getInt("reservation_id"));
        evaluation.setChambreId(rs.getInt("chambre_id"));
        evaluation.setUserId(rs.getInt("user_id"));
        evaluation.setNote(rs.getInt("note"));
        evaluation.setCommentaire(rs.getString("commentaire"));
        evaluation.setDateEvaluation(rs.getTimestamp("date_evaluation").toLocalDateTime());
        return evaluation;
    }
}
