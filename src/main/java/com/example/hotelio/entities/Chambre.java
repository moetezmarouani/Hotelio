package com.example.hotelio.entities;

import com.example.hotelio.enums.StatutChambre;
import com.example.hotelio.enums.TypeChambre;

import java.time.LocalDateTime;

public class Chambre {
    private int id;
    private String numero;
    private TypeChambre type;
    private double prixParNuit;
    private int capacite;
    private String description;
    private StatutChambre statut;
    private int etage;
    private LocalDateTime dateCreation;

    public Chambre() {}

    public Chambre(String numero, TypeChambre type, double prixParNuit,
                   int capacite, String description, int etage) {
        this.numero = numero;
        this.type = type;
        this.prixParNuit = prixParNuit;
        this.capacite = capacite;
        this.description = description;
        this.etage = etage;
        this.statut = StatutChambre.DISPONIBLE;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public TypeChambre getType() { return type; }
    public void setType(TypeChambre type) { this.type = type; }

    public double getPrixParNuit() { return prixParNuit; }
    public void setPrixParNuit(double prixParNuit) { this.prixParNuit = prixParNuit; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public StatutChambre getStatut() { return statut; }
    public void setStatut(StatutChambre statut) { this.statut = statut; }

    public int getEtage() { return etage; }
    public void setEtage(int etage) { this.etage = etage; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "Chambre " + numero + " - " + type + " (" + prixParNuit + "€/nuit)";
    }
}

