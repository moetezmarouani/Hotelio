package com.example.hotelio.controllers;

import com.example.hotelio.entities.*;
import com.example.hotelio.enums.StatutReservation;
import com.example.hotelio.services.FideliteService;
import com.example.hotelio.services.ReservationService;
import com.example.hotelio.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MonProfilController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private Label dateCreationLabel;
    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;

    @FXML private Label totalReservationsLabel;
    @FXML private Label totalNuitsLabel;
    @FXML private Label totalDepenseLabel;
    @FXML private Label niveauLabel;
    @FXML private Label pointsLabel;
    @FXML private Label reductionLabel;
    @FXML private Label prochainNiveauLabel;
    @FXML private ProgressBar progressBar;

    private UserService userService = new UserService();
    private ReservationService reservationService = new ReservationService();
    private FideliteService fideliteService = new FideliteService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        loadUserData();
        loadStatistics();
        loadFideliteData();
    }

    private void loadUserData() {
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateCreationLabel.setText(currentUser.getDateCreation().format(formatter));
    }

    private void loadStatistics() {
        List<Reservation> reservations = reservationService.obtenirReservationsParUtilisateur(currentUser.getId());

        // Nombre total de réservations
        long totalReservations = reservations.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .count();
        totalReservationsLabel.setText(String.valueOf(totalReservations));

        // Nombre total de nuits
        long totalNuits = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.TERMINEE)
                .mapToLong(Reservation::getNombreNuits)
                .sum();
        totalNuitsLabel.setText(String.valueOf(totalNuits));

        // Total dépensé
        double totalDepense = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.TERMINEE ||
                        r.getStatut() == StatutReservation.CONFIRMEE)
                .mapToDouble(Reservation::getPrixTotal)
                .sum();
        totalDepenseLabel.setText(String.format("%.2f DT", totalDepense));
    }

    private void loadFideliteData() {
        int points = fideliteService.obtenirPoints(currentUser.getId());
        String niveau = fideliteService.obtenirNiveau(currentUser.getId());
        double reduction = fideliteService.obtenirReduction(niveau);

        // Afficher les points
        pointsLabel.setText(String.valueOf(points));

        // Afficher le niveau avec emoji et couleur
        String emoji = fideliteService.getEmojiNiveau(niveau);
        niveauLabel.setText(emoji + " " + niveau);
        String couleur = fideliteService.getCouleurNiveau(niveau);
        niveauLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");

        // Afficher la réduction
        reductionLabel.setText(String.format("%.0f%%", reduction));

        // Définir les seuils pour chaque niveau
        int pointsMin = 0, pointsMax = 0;
        switch(niveau.toUpperCase()) {
            case "BRONZE": pointsMin = 0; pointsMax = 1000; break;
            case "SILVER": pointsMin = 1000; pointsMax = 3000; break;
            case "GOLD": pointsMin = 3000; pointsMax = 5000; break;
            case "PLATINUM": pointsMin = 5000; pointsMax = 5000; break; // niveau max
        }

        // Calculer la progression
        double progression = (pointsMax != pointsMin) ? (double)(points - pointsMin) / (pointsMax - pointsMin) : 1.0;
        progressBar.setProgress(Math.min(progression, 1.0));

        // Prochain niveau
        if(niveau.equalsIgnoreCase("PLATINUM")) {
            prochainNiveauLabel.setText("🎉 Vous avez atteint le niveau maximum !");
        } else {
            String prochainNiv = fideliteService.prochainNiveau(niveau);
            int pointsNecessaires = pointsMax - points;
            prochainNiveauLabel.setText(
                    String.format("Plus que %d points pour atteindre %s %s",
                            pointsNecessaires,
                            fideliteService.getEmojiNiveau(prochainNiv),
                            prochainNiv)
            );
        }
    }

    @FXML
    private void handleUpdate() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Les champs nom, prénom et email sont obligatoires!");
            return;
        }

        // Vérifier si l'email a changé et s'il existe déjà
        if (!email.equals(currentUser.getEmail())) {
            // Vérification simplifiée - dans un vrai projet, vérifier en base
            List<User> users = userService.obtenirTousLesUtilisateurs();
            boolean emailExiste = users.stream()
                    .anyMatch(u -> u.getEmail().equals(email) && u.getId() != currentUser.getId());

            if (emailExiste) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Cet email est déjà utilisé par un autre utilisateur!");
                return;
            }
        }

        // Mettre à jour l'utilisateur
        currentUser.setNom(nom);
        currentUser.setPrenom(prenom);
        currentUser.setEmail(email);
        currentUser.setTelephone(telephone);

        if (userService.modifierUtilisateur(currentUser)) {
            // Mettre à jour la session
            SessionManager.setCurrentUser(currentUser);

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Profil mis à jour avec succès!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de mettre à jour le profil.");
        }
    }

    @FXML
    private void handleCancel() {
        loadUserData();
    }

    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez remplir tous les champs!");
            return;
        }

        // Vérifier l'ancien mot de passe
        if (!oldPassword.equals(currentUser.getMotDePasse())) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "L'ancien mot de passe est incorrect!");
            return;
        }

        // Vérifier la confirmation
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Les nouveaux mots de passe ne correspondent pas!");
            return;
        }

        // Vérifier la longueur minimale
        if (newPassword.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Le mot de passe doit contenir au moins 6 caractères!");
            return;
        }

        // Mettre à jour le mot de passe
        currentUser.setMotDePasse(newPassword);

        if (userService.modifierUtilisateur(currentUser)) {
            SessionManager.setCurrentUser(currentUser);

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Mot de passe modifié avec succès!");

            // Effacer les champs
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de modifier le mot de passe.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}