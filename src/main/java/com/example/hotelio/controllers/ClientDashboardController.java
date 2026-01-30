package com.example.hotelio.controllers;

import com.example.hotelio.entities.User;
import com.example.hotelio.utils.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class ClientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private Button rechercheButton;
    @FXML private Button reservationsButton;
    @FXML private Button profilButton;
    @FXML private StackPane centerPane;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("👋 Bienvenue, " + currentUser.getNomComplet());
        }

        // Ajouter des effets hover aux boutons de menu
        addHoverEffect(rechercheButton);
        addHoverEffect(reservationsButton);
        addHoverEffect(profilButton);
    }

    @FXML
    private void handleShowRecherche() {
        loadView("/fxml/RechercheChambres.fxml");
    }

    @FXML
    private void handleShowReservations() {
        loadView("/fxml/MesReservations.fxml");
    }

    @FXML
    private void handleShowProfil() {
        loadView("/fxml/MonProfil.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            centerPane.getChildren().clear();
            centerPane.getChildren().add(loader.load());
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de charger la vue: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearCurrentUser();
        NavigationUtil.navigateTo(logoutButton, "/fxml/Login.fxml", "HotelIO - Connexion");
    }

    private void addHoverEffect(Button button) {
        String normalStyle = button.getStyle();
        String hoverStyle = "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10; -fx-cursor: hand; -fx-alignment: center-left;";

        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }
}