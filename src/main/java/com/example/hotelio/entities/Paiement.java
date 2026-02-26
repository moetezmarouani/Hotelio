package com.example.hotelio.entities;


import com.example.hotelio.enums.StatutPaiement;
import com.example.hotelio.enums.TypePaiement;
import java.time.LocalDateTime;

public class Paiement {
    private int id;
    private int reservationId;
    private double montant;
    private TypePaiement typePaiement;
    private StatutPaiement statut;
    private String numeroTransaction;
    private String numeroCarteMasque;
    private LocalDateTime datePaiement;

    public Paiement() {
        this.datePaiement = LocalDateTime.now();
        this.statut = StatutPaiement.EN_ATTENTE;
    }

    public Paiement(int reservationId, double montant, TypePaiement typePaiement) {
        this();
        this.reservationId = reservationId;
        this.montant = montant;
        this.typePaiement = typePaiement;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public TypePaiement getTypePaiement() { return typePaiement; }
    public void setTypePaiement(TypePaiement typePaiement) { this.typePaiement = typePaiement; }

    public StatutPaiement getStatut() { return statut; }
    public void setStatut(StatutPaiement statut) { this.statut = statut; }

    public String getNumeroTransaction() { return numeroTransaction; }
    public void setNumeroTransaction(String numeroTransaction) {
        this.numeroTransaction = numeroTransaction;
    }

    public String getNumeroCarteMasque() { return numeroCarteMasque; }
    public void setNumeroCarteMasque(String numeroCarteMasque) {
        this.numeroCarteMasque = numeroCarteMasque;
    }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    @Override
    public String toString() {
        return "Paiement #" + id + " - " + montant + " DT - " + statut;
    }
}