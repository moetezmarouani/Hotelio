package com.example.hotelio.controllers;

import com.example.hotelio.entities.User;
import com.example.hotelio.utils.NavigationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private Button usersButton;
    @FXML private Button chambresButton;
    @FXML private Button reservationsButton;
    @FXML private StackPane centerPane;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("👋 Bienvenue, " + currentUser.getNomComplet());
        }

        // Ajouter des effets hover aux boutons de menu
        addHoverEffect(usersButton);
        addHoverEffect(chambresButton);
        addHoverEffect(reservationsButton);
    }

    @FXML
    private void handleShowUsers() {
        loadView("/fxml/UserManagement.fxml");
    }

    @FXML
    private void handleShowChambres() {
        loadView("/fxml/ChambreManagement.fxml");
    }

    @FXML
    private void handleShowReservations() {
        // Pour l'instant, afficher un message
        centerPane.getChildren().clear();
        Label label = new Label("Gestion des Réservations (À implémenter)");
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        centerPane.getChildren().add(label);
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearCurrentUser();
        NavigationUtil.navigateTo(logoutButton, "/fxml/Login.fxml", "HotelIO - Connexion");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            centerPane.getChildren().clear();
            centerPane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de charger la vue: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void addHoverEffect(Button button) {
        String normalStyle = button.getStyle();
        String hoverStyle = "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10; -fx-cursor: hand; -fx-alignment: center-left;";

        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }
}