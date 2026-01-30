package com.example.hotelio.entities;

public class Client extends User {

    // Attributs spécifiques au client
    private String preferences;
    private int nombreReservations;
    private boolean fidele; // Client fidèle après X réservations

    public Client() {
        super();
    }

    public Client(String nom, String prenom, String email, String telephone, String motDePasse) {
        super(nom, prenom, email, telephone, motDePasse);
        this.nombreReservations = 0;
        this.fidele = false;
    }

    @Override
    public String getTypeUtilisateur() {
        return "CLIENT";
    }

    // Méthodes spécifiques au client
    public void incrementerReservations() {
        this.nombreReservations++;
        if (this.nombreReservations >= 5) {
            this.fidele = true;
        }
    }

    public boolean peutAvoirReduction() {
        return this.fidele;
    }

    // Getters et Setters spécifiques
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public int getNombreReservations() { return nombreReservations; }
    public void setNombreReservations(int nombreReservations) {
        this.nombreReservations = nombreReservations;
        if (nombreReservations >= 5) {
            this.fidele = true;
        }
    }

    public boolean isFidele() { return fidele; }
    public void setFidele(boolean fidele) { this.fidele = fidele; }

    @Override
    public String toString() {
        return super.toString() + " [CLIENT" + (fidele ? " - FIDÈLE" : "") + "]";
    }
}