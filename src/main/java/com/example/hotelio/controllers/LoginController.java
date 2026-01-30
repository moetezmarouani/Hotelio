package com.example.hotelio.controllers;

import com.example.hotelio.entities.*;
import com.example.hotelio.services.UserService;
import com.example.hotelio.utils.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Code d'initialisation si nécessaire
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez remplir tous les champs!");
            return;
        }

        User user = userService.authentifier(email, password);

        if (user != null) {
            // Stocker l'utilisateur connecté
            SessionManager.setCurrentUser(user);

            // Rediriger selon le type d'utilisateur
            if (user instanceof Admin) {
                NavigationUtil.navigateTo(loginButton, "/fxml/AdminDashboard.fxml", "HotelIO - Admin");
            } else if (user instanceof Client) {
                NavigationUtil.navigateTo(loginButton, "/fxml/ClientDashboard.fxml", "HotelIO - Client");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Email ou mot de passe incorrect!");
        }
    }

    @FXML
    private void handleGoToRegister() {
        NavigationUtil.navigateTo(registerButton, "/fxml/Register.fxml", "HotelIO - Inscription");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}