package com.example.hotelio.services;

import com.example.hotelio.entities.Paiement;
import com.example.hotelio.enums.StatutPaiement;
import com.example.hotelio.enums.TypePaiement;
import com.example.hotelio.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaiementService {

    /**
     * Valide un numéro de carte avec l'algorithme de Luhn
     */
    public boolean validerNumeroCarte(String numero) {
        // Enlever les espaces
        numero = numero.replaceAll("\\s+", "");

        // Vérifier que c'est bien des chiffres
        if (!numero.matches("\\d+")) {
            return false;
        }

        // Vérifier la longueur (13-19 chiffres)
        if (numero.length() < 13 || numero.length() > 19) {
            return false;
        }

        // Algorithme de Luhn
        int sum = 0;
        boolean alternate = false;

        for (int i = numero.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(numero.substring(i, i + 1));

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }

            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Détermine le type de carte (Visa, Mastercard, etc.)
     */
    public String determinerTypeCarte(String numero) {
        numero = numero.replaceAll("\\s+", "");

        if (numero.startsWith("4")) {
            return "Visa";
        } else if (numero.matches("^5[1-5].*")) {
            return "Mastercard";
        } else if (numero.matches("^3[47].*")) {
            return "American Express";
        } else {
            return "Inconnu";
        }
    }

    /**
     * Valide le CVV
     */
    public boolean validerCVV(String cvv, String typeCarte) {
        if (!cvv.matches("\\d+")) {
            return false;
        }

        // American Express = 4 chiffres, autres = 3 chiffres
        if ("American Express".equals(typeCarte)) {
            return cvv.length() == 4;
        } else {
            return cvv.length() == 3;
        }
    }

    /**
     * Simule un paiement (en production, intégrer une vraie API de paiement)
     */
    public boolean traiterPaiement(Paiement paiement) {
        try {
            // Simulation: 95% de succès
            boolean succes = Math.random() < 0.95;

            if (succes) {
                paiement.setStatut(StatutPaiement.VALIDE);
                paiement.setNumeroTransaction("TXN-" + System.currentTimeMillis());
            } else {
                paiement.setStatut(StatutPaiement.REFUSE);
            }

            // Enregistrer en base de données
            return enregistrerPaiement(paiement);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Enregistre un paiement dans la base de données
     */
    private boolean enregistrerPaiement(Paiement paiement) {
        String sql = "INSERT INTO paiements (reservation_id, montant, type_paiement, " +
                "statut, numero_transaction, numero_carte_masque, date_paiement) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, paiement.getReservationId());
            stmt.setDouble(2, paiement.getMontant());
            stmt.setString(3, paiement.getTypePaiement().name());
            stmt.setString(4, paiement.getStatut().name());
            stmt.setString(5, paiement.getNumeroTransaction());
            stmt.setString(6, paiement.getNumeroCarteMasque());
            stmt.setTimestamp(7, Timestamp.valueOf(paiement.getDatePaiement()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        paiement.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erreur enregistrement paiement: " + e.getMessage());
        }

        return false;
    }

    /**
     * Masque le numéro de carte (garde seulement les 4 derniers chiffres)
     */
    public String masquerNumeroCarte(String numero) {
        numero = numero.replaceAll("\\s+", "");
        if (numero.length() < 4) {
            return "****";
        }
        String derniers4 = numero.substring(numero.length() - 4);
        return "**** **** **** " + derniers4;
    }

    /**
     * Récupère les paiements d'une réservation
     */
    public List<Paiement> obtenirPaiementsParReservation(int reservationId) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiements WHERE reservation_id = ? ORDER BY date_paiement DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    paiements.add(mapResultSetToPaiement(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération paiements: " + e.getMessage());
        }

        return paiements;
    }

    /**
     * Vérifie si une réservation est payée
     */
    public boolean estReservationPayee(int reservationId) {
        String sql = "SELECT COUNT(*) FROM paiements WHERE reservation_id = ? AND statut = 'VALIDE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification paiement: " + e.getMessage());
        }

        return false;
    }

    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setId(rs.getInt("id"));
        paiement.setReservationId(rs.getInt("reservation_id"));
        paiement.setMontant(rs.getDouble("montant"));
        paiement.setTypePaiement(TypePaiement.valueOf(rs.getString("type_paiement")));
        paiement.setStatut(StatutPaiement.valueOf(rs.getString("statut")));
        paiement.setNumeroTransaction(rs.getString("numero_transaction"));
        paiement.setNumeroCarteMasque(rs.getString("numero_carte_masque"));
        paiement.setDatePaiement(rs.getTimestamp("date_paiement").toLocalDateTime());
        return paiement;
    }
}