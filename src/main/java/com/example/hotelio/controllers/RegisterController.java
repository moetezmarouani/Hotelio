package com.example.hotelio.controllers;

import com.example.hotelio.entities.Client;
import com.example.hotelio.services.UserService;
import com.example.hotelio.utils.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backButton;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Code d'initialisation si nécessaire
    }

    @FXML
    private void handleRegister() {
        if (nomField.getText().trim().isEmpty() ||
                prenomField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                passwordField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez remplir tous les champs obligatoires!");
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Les mots de passe ne correspondent pas!");
            return;
        }

        // Créer un nouveau client
        Client newClient = new Client(
                nomField.getText().trim(),
                prenomField.getText().trim(),
                emailField.getText().trim(),
                telephoneField.getText().trim(),
                passwordField.getText().trim()
        );

        if (userService.creerUtilisateur(newClient)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Compte créé avec succès! Vous pouvez maintenant vous connecter.");
            handleGoToLogin();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la création du compte. L'email existe peut-être déjà.");
        }
    }

    @FXML
    private void handleGoToLogin() {
        NavigationUtil.navigateTo(backButton, "/fxml/Login.fxml", "HotelIO - Connexion");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}