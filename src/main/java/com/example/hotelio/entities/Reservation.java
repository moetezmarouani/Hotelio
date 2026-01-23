package com.example.hotelio.entities;

import com.example.hotelio.enums.StatutReservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int userId;
    private int chambreId;
    private LocalDate dateCheckin;
    private LocalDate dateCheckout;
    private int nombrePersonnes;
    private double prixTotal;
    private StatutReservation statut;
    private String commentaire;
    private LocalDateTime dateReservation;

    // Pour l'affichage (jointures)
    private User user;
    private Chambre chambre;

    public Reservation() {}

    public Reservation(int userId, int chambreId, LocalDate dateCheckin,
                       LocalDate dateCheckout, int nombrePersonnes, double prixTotal) {
        this.userId = userId;
        this.chambreId = chambreId;
        this.dateCheckin = dateCheckin;
        this.dateCheckout = dateCheckout;
        this.nombrePersonnes = nombrePersonnes;
        this.prixTotal = prixTotal;
        this.statut = StatutReservation.EN_ATTENTE;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getChambreId() { return chambreId; }
    public void setChambreId(int chambreId) { this.chambreId = chambreId; }

    public LocalDate getDateCheckin() { return dateCheckin; }
    public void setDateCheckin(LocalDate dateCheckin) { this.dateCheckin = dateCheckin; }

    public LocalDate getDateCheckout() { return dateCheckout; }
    public void setDateCheckout(LocalDate dateCheckout) {
        this.dateCheckout = dateCheckout;
    }

    public int getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(int nombrePersonnes) {
        this.nombrePersonnes = nombrePersonnes;
    }

    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double prixTotal) { this.prixTotal = prixTotal; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Chambre getChambre() { return chambre; }
    public void setChambre(Chambre chambre) { this.chambre = chambre; }

    public long getNombreNuits() {
        return java.time.temporal.ChronoUnit.DAYS.between(dateCheckin, dateCheckout);
    }

    @Override
    public String toString() {
        return "Réservation #" + id + " - " + dateCheckin + " au " + dateCheckout;
    }
}
