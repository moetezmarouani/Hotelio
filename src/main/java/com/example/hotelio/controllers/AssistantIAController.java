package com.example.hotelio.controllers;

import com.example.hotelio.services.GeminiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;

public class AssistantIAController {

    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatContainer;
    @FXML private TextArea messageInput;
    @FXML private Button envoyerButton;
    @FXML private Button nouveauChatButton;
    @FXML private Label statusLabel;

    private GeminiService geminiService;
    private boolean enCoursDeTraitement = false;

    @FXML
    public void initialize() {
        geminiService = new GeminiService();

        // Auto-scroll vers le bas
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            chatScrollPane.setVvalue(1.0);
        });

        // Enter pour envoyer (Shift+Enter pour nouvelle ligne)
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER") && !event.isShiftDown()) {
                event.consume();
                handleEnvoyer();
            }
        });
    }

    @FXML
    private void handleEnvoyer() {
        String message = messageInput.getText().trim();

        if (message.isEmpty() || enCoursDeTraitement) {
            return;
        }

        // Afficher le message de l'utilisateur
        ajouterMessageUtilisateur(message);
        messageInput.clear();

        // Désactiver l'envoi pendant le traitement
        enCoursDeTraitement = true;
        envoyerButton.setDisable(true);
        messageInput.setDisable(true);
        statusLabel.setText("🤖 L'IA réfléchit...");

        // Traiter la question dans un thread séparé
        new Thread(() -> {
            try {
                String reponse = geminiService.obtenirReponse(message);

                // Afficher la réponse dans le thread JavaFX
                Platform.runLater(() -> {
                    ajouterMessageIA(reponse);
                    statusLabel.setText("Prêt");
                    enCoursDeTraitement = false;
                    envoyerButton.setDisable(false);
                    messageInput.setDisable(false);
                    messageInput.requestFocus();
                });

            } catch (IOException e) {
                Platform.runLater(() -> {
                    ajouterMessageErreur("Erreur de connexion à l'IA. Veuillez réessayer.");
                    statusLabel.setText("Erreur");
                    enCoursDeTraitement = false;
                    envoyerButton.setDisable(false);
                    messageInput.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleNouveauChat() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Nouvelle conversation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous démarrer une nouvelle conversation ? L'historique actuel sera perdu.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                chatContainer.getChildren().clear();
                geminiService.reinitialiserConversation();

                // Message de bienvenue
                ajouterMessageIA("Bonjour ! Je suis votre assistant virtuel. Comment puis-je vous aider à trouver la chambre parfaite pour votre séjour ?");
            }
        });
    }

    @FXML
    private void handleSuggestion1() {
        messageInput.setText("Je cherche une chambre pour un couple. Quelles options avez-vous ?");
        handleEnvoyer();
    }

    @FXML
    private void handleSuggestion2() {
        messageInput.setText("Quelles chambres sont disponibles pour un budget de moins de 150 DT par nuit ?");
        handleEnvoyer();
    }

    @FXML
    private void handleSuggestion3() {
        messageInput.setText("Je voyage avec ma famille (4 personnes). Quelle chambre recommandez-vous ?");
        handleEnvoyer();
    }

    /**
     * Ajoute un message de l'utilisateur au chat
     */
    private void ajouterMessageUtilisateur(String message) {
        VBox messageBox = new VBox(5);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5));

        VBox bubble = new VBox(5);
        bubble.setAlignment(Pos.CENTER_RIGHT);
        bubble.setStyle("-fx-background-color: #667eea; -fx-padding: 12; -fx-background-radius: 15 15 0 15; -fx-max-width: 450;");

        Label userLabel = new Label("👤 Vous");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: white;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        bubble.getChildren().addAll(userLabel, messageLabel);
        messageBox.getChildren().add(bubble);

        chatContainer.getChildren().add(messageBox);
    }

    /**
     * Ajoute une réponse de l'IA au chat
     */
    private void ajouterMessageIA(String message) {
        VBox messageBox = new VBox(5);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5));

        VBox bubble = new VBox(5);
        bubble.setAlignment(Pos.CENTER_LEFT);
        bubble.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 12; -fx-background-radius: 15 15 15 0; -fx-max-width: 450;");

        Label aiLabel = new Label("🤖 Assistant IA");
        aiLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #667eea;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        bubble.getChildren().addAll(aiLabel, messageLabel);
        messageBox.getChildren().add(bubble);

        chatContainer.getChildren().add(messageBox);
    }

    /**
     * Ajoute un message d'erreur au chat
     */
    private void ajouterMessageErreur(String message) {
        VBox messageBox = new VBox(5);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(5));

        Label errorLabel = new Label("⚠️ " + message);
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; -fx-padding: 10; " +
                "-fx-background-color: #ffebee; -fx-background-radius: 5;");

        messageBox.getChildren().add(errorLabel);
        chatContainer.getChildren().add(messageBox);
    }
}