package com.example.hotelio.entities;

import java.time.LocalDateTime;

public class Evaluation {
    private int id;
    private int reservationId;
    private int chambreId;
    private int userId;
    private int note;
    private String commentaire;
    private LocalDateTime dateEvaluation;

    public Evaluation() {}

    public Evaluation(int reservationId, int chambreId, int userId,
                      int note, String commentaire) {
        this.reservationId = reservationId;
        this.chambreId = chambreId;
        this.userId = userId;
        this.note = note;
        this.commentaire = commentaire;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getChambreId() { return chambreId; }
    public void setChambreId(int chambreId) { this.chambreId = chambreId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getNote() { return note; }
    public void setNote(int note) {
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }
        this.note = note;
    }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDateTime dateEvaluation) {
        this.dateEvaluation = dateEvaluation;
    }

    @Override
    public String toString() {
        return note + "/5 - " + commentaire;
    }
}
