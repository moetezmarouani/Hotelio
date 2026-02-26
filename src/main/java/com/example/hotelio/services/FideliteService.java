package com.example.hotelio.services;

import com.example.hotelio.entities.User;
import com.example.hotelio.entities.Client;
import com.example.hotelio.utils.DatabaseConnection;
import java.sql.*;

public class FideliteService {

    /**
     * Calcule les points gagnés pour une réservation
     * Règle : 10 points par DT dépensé
     */
    public int calculerPointsGagnes(double montant) {
        return (int) (montant * 10);
    }

    /**
     * Ajoute des points au client
     */
    public boolean ajouterPoints(int userId, int points) {
        String sql = "UPDATE users SET points_fidelite = points_fidelite + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, points);
            stmt.setInt(2, userId);

            boolean success = stmt.executeUpdate() > 0;

            if (success) {
                // Mettre à jour le niveau après ajout de points
                mettreAJourNiveau(userId);
            }

            return success;

        } catch (SQLException e) {
            System.err.println("Erreur ajout points: " + e.getMessage());
        }

        return false;
    }

    /**
     * Récupère les points d'un utilisateur
     */
    public int obtenirPoints(int userId) {
        String sql = "SELECT points_fidelite FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("points_fidelite");
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération points: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Récupère le niveau de fidélité d'un utilisateur
     */
    public String obtenirNiveau(int userId) {
        String sql = "SELECT niveau_fidelite FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("niveau_fidelite");
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération niveau: " + e.getMessage());
        }

        return "BRONZE";
    }

    /**
     * Met à jour le niveau de fidélité selon les points
     * BRONZE: 0-999 points
     * SILVER: 1000-2999 points
     * GOLD: 3000-4999 points
     * PLATINUM: 5000+ points
     */
    public boolean mettreAJourNiveau(int userId) {
        int points = obtenirPoints(userId);
        String niveau;

        if (points >= 5000) {
            niveau = "PLATINUM";
        } else if (points >= 3000) {
            niveau = "GOLD";
        } else if (points >= 1000) {
            niveau = "SILVER";
        } else {
            niveau = "BRONZE";
        }

        String sql = "UPDATE users SET niveau_fidelite = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, niveau);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur mise à jour niveau: " + e.getMessage());
        }

        return false;
    }

    /**
     * Calcule le pourcentage de réduction selon le niveau
     */
    public double obtenirReduction(String niveau) {
        switch (niveau) {
            case "SILVER":
                return 5.0;  // 5%
            case "GOLD":
                return 10.0; // 10%
            case "PLATINUM":
                return 15.0; // 15%
            default:
                return 0.0;  // BRONZE = pas de réduction
        }
    }

    /**
     * Applique une réduction selon le niveau de fidélité
     */
    public double appliquerReduction(double montant, String niveau) {
        double reduction = obtenirReduction(niveau);
        return montant * (1 - reduction / 100);
    }

    /**
     * Points nécessaires pour le prochain niveau
     */
    public int pointsPourProchainNiveau(int pointsActuels) {
        if (pointsActuels < 1000) {
            return 1000 - pointsActuels; // Pour SILVER
        } else if (pointsActuels < 3000) {
            return 3000 - pointsActuels; // Pour GOLD
        } else if (pointsActuels < 5000) {
            return 5000 - pointsActuels; // Pour PLATINUM
        } else {
            return 0; // Déjà au max
        }
    }

    /**
     * Nom du prochain niveau
     */
    public String prochainNiveau(String niveauActuel) {
        switch (niveauActuel) {
            case "BRONZE":
                return "SILVER";
            case "SILVER":
                return "GOLD";
            case "GOLD":
                return "PLATINUM";
            default:
                return "MAX";
        }
    }

    /**
     * Vérifie si le client a eu une promotion de niveau
     */
    public boolean aEuPromotion(int userId, String ancienNiveau) {
        String nouveauNiveau = obtenirNiveau(userId);
        return !ancienNiveau.equals(nouveauNiveau) &&
                getNiveauOrdre(nouveauNiveau) > getNiveauOrdre(ancienNiveau);
    }

    private int getNiveauOrdre(String niveau) {
        switch (niveau) {
            case "BRONZE": return 1;
            case "SILVER": return 2;
            case "GOLD": return 3;
            case "PLATINUM": return 4;
            default: return 0;
        }
    }

    /**
     * Emoji pour le niveau
     */
    public String getEmojiNiveau(String niveau) {
        switch (niveau) {
            case "BRONZE": return "🥉";
            case "SILVER": return "🥈";
            case "GOLD": return "🥇";
            case "PLATINUM": return "💎";
            default: return "⭐";
        }
    }

    /**
     * Couleur pour le niveau (hex)
     */
    public String getCouleurNiveau(String niveau) {
        switch (niveau) {
            case "BRONZE": return "#CD7F32";
            case "SILVER": return "#C0C0C0";
            case "GOLD": return "#FFD700";
            case "PLATINUM": return "#E5E4E2";
            default: return "#95a5a6";
        }
    }
}