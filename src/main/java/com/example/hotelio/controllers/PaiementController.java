package com.example.hotelio.controllers;

import com.example.hotelio.entities.Paiement;
import com.example.hotelio.enums.StatutPaiement;
import com.example.hotelio.enums.TypePaiement;
import com.example.hotelio.services.PaiementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

public class PaiementController {

    @FXML private Label montantLabel;
    @FXML private TextField numeroCarteField;
    @FXML private Label carteTypeLabel;
    @FXML private TextField nomTitulaireField;
    @FXML private ComboBox<Integer> moisComboBox;
    @FXML private ComboBox<Integer> anneeComboBox;
    @FXML private TextField cvvField;
    @FXML private Button payerButton;
    @FXML private Button annulerButton;

    private final PaiementService paiementService = new PaiementService();

    private int reservationId;
    private double montant;

    // 🔹 Initialisation
    @FXML
    public void initialize() {

        // Remplir mois 1-12
        moisComboBox.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 12).boxed().toList()
        ));

        // Remplir années (année actuelle + 10)
        int currentYear = LocalDate.now().getYear();
        anneeComboBox.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(currentYear, currentYear + 10).boxed().toList()
        ));

        // Détection automatique type carte
        numeroCarteField.textProperty().addListener((obs, oldVal, newVal) -> {
            String type = paiementService.determinerTypeCarte(newVal);
            carteTypeLabel.setText(type.equals("Inconnu") ? "" : "Type: " + type);
        });

        payerButton.setOnAction(e -> handlePaiement());
        annulerButton.setOnAction(e -> fermerFenetre());
    }

    // 🔹 Méthode appelée depuis l'extérieur pour injecter données
    public void setPaiementData(int reservationId, double montant) {
        this.reservationId = reservationId;
        this.montant = montant;
        montantLabel.setText(String.format("Montant à payer: %.2f DT", montant));
    }

    // 🔹 Traitement paiement
    private void handlePaiement() {

        String numeroCarte = numeroCarteField.getText().trim();
        String nom = nomTitulaireField.getText().trim();
        Integer mois = moisComboBox.getValue();
        Integer annee = anneeComboBox.getValue();
        String cvv = cvvField.getText().trim();

        // 🔎 Validation basique
        if (numeroCarte.isEmpty() || nom.isEmpty() ||
                mois == null || annee == null || cvv.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur",
                    "Veuillez remplir tous les champs.");
            return;
        }

        // 🔎 Validation numéro carte
        if (!paiementService.validerNumeroCarte(numeroCarte)) {
            showAlert(Alert.AlertType.ERROR, "Carte invalide",
                    "Numéro de carte invalide.");
            return;
        }

        String typeCarte = paiementService.determinerTypeCarte(numeroCarte);

        if (!paiementService.validerCVV(cvv, typeCarte)) {
            showAlert(Alert.AlertType.ERROR, "CVV invalide",
                    "Code CVV invalide.");
            return;
        }

        // 🔎 Vérification expiration
        LocalDate expiration = LocalDate.of(annee, mois, 1).withDayOfMonth(
                LocalDate.of(annee, mois, 1).lengthOfMonth()
        );

        if (expiration.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Carte expirée",
                    "Votre carte est expirée.");
            return;
        }

        // 🔹 Création paiement
        Paiement paiement = new Paiement();
        paiement.setReservationId(reservationId);
        paiement.setMontant(montant);
        paiement.setTypePaiement(TypePaiement.CARTE_BANCAIRE);
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        paiement.setNumeroCarteMasque(
                paiementService.masquerNumeroCarte(numeroCarte)
        );
        paiement.setDatePaiement(LocalDateTime.now());

        boolean succes = paiementService.traiterPaiement(paiement);

        if (succes && paiement.getStatut() == StatutPaiement.VALIDE) {
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Paiement validé !\nTransaction: " +
                            paiement.getNumeroTransaction());
            fermerFenetre();
        } else {
            showAlert(Alert.AlertType.ERROR, "Paiement refusé",
                    "Le paiement a été refusé.");
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) payerButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}