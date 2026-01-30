package com.example.hotelio.entities;

import java.time.LocalDateTime;

public class Admin extends User {

    // Attributs spécifiques à l'admin
    private String departement;
    private String niveau; // SUPER_ADMIN, ADMIN, GESTIONNAIRE
    private LocalDateTime dernierAcces;
    private boolean superAdmin;

    public Admin() {
        super();
        this.niveau = "ADMIN";
        this.superAdmin = false;
    }

    public Admin(String nom, String prenom, String email, String telephone, String motDePasse) {
        super(nom, prenom, email, telephone, motDePasse);
        this.niveau = "ADMIN";
        this.superAdmin = false;
    }

    @Override
    public String getTypeUtilisateur() {
        return "ADMIN";
    }

    // Méthodes spécifiques à l'admin
    public void mettreAJourAcces() {
        this.dernierAcces = LocalDateTime.now();
    }

    public boolean peutGererUtilisateurs() {
        return true; // Tous les admins peuvent gérer les utilisateurs
    }

    public boolean peutGererParametres() {
        return this.superAdmin; // Seuls les super admins peuvent gérer les paramètres
    }

    // Getters et Setters spécifiques
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public LocalDateTime getDernierAcces() { return dernierAcces; }
    public void setDernierAcces(LocalDateTime dernierAcces) {
        this.dernierAcces = dernierAcces;
    }

    public boolean isSuperAdmin() { return superAdmin; }
    public void setSuperAdmin(boolean superAdmin) { this.superAdmin = superAdmin; }

    @Override
    public String toString() {
        return super.toString() + " [ADMIN - " + niveau + "]";
    }
}