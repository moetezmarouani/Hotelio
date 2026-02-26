package com.example.hotelio.controllers;

import com.example.hotelio.entities.*;
import com.example.hotelio.enums.StatutReservation;
import com.example.hotelio.services.ChambreService;
import com.example.hotelio.services.EvaluationService;
import com.example.hotelio.services.PdfService;
import com.example.hotelio.services.ReservationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MesReservationsController {

    @FXML private Label statsLabel;
    @FXML private ComboBox<String> statutFilterComboBox;
    @FXML private Button refreshButton;
    @FXML private TableView<Reservation> reservationsTableView;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> chambreColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkinColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkoutColumn;
    @FXML private TableColumn<Reservation, Integer> nuitsColumn;
    @FXML private TableColumn<Reservation, Integer> personnesColumn;
    @FXML private TableColumn<Reservation, Double> prixColumn;
    @FXML private TableColumn<Reservation, StatutReservation> statutColumn;
    @FXML private TableColumn<Reservation, Void> actionsColumn;

    private ReservationService reservationService = new ReservationService();
    private ChambreService chambreService = new ChambreService();
    private EvaluationService evaluationService = new EvaluationService();
    private PdfService pdfService = new PdfService();
    private List<Reservation> toutesLesReservations;

    @FXML
    public void initialize() {
        setupFilterComboBox();
        setupTableColumns();
        loadReservations();
    }

    private void setupFilterComboBox() {
        statutFilterComboBox.getItems().add("Tous");
        for (StatutReservation statut : StatutReservation.values()) {
            statutFilterComboBox.getItems().add(statut.name());
        }
        statutFilterComboBox.setValue("Tous");
    }

    private void setupTableColumns() {
        // Colonne chambre (afficher le numéro)
        chambreColumn.setCellValueFactory(cellData -> {
            Chambre chambre = chambreService.obtenirChambreParId(cellData.getValue().getChambreId());
            return new javafx.beans.property.SimpleStringProperty(
                    chambre != null ? chambre.getNumero() + " - " + chambre.getType() : "N/A"
            );
        });

        // Colonne nombre de nuits
        nuitsColumn.setCellValueFactory(cellData -> {
            long nuits = cellData.getValue().getNombreNuits();
            return new javafx.beans.property.SimpleObjectProperty<>((int) nuits);
        });

        // Format de la colonne prix
        prixColumn.setCellFactory(col -> new TableCell<Reservation, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", prix));
                }
            }
        });

        // Colonne statut avec style
        statutColumn.setCellFactory(col -> new TableCell<Reservation, StatutReservation>() {
            @Override
            protected void updateItem(StatutReservation statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut.name());
                    String style = "-fx-padding: 5; -fx-background-radius: 5; -fx-font-weight: bold; ";
                    switch (statut) {
                        case CONFIRMEE:
                            style += "-fx-background-color: #27ae60; -fx-text-fill: white;";
                            break;
                        case EN_ATTENTE:
                            style += "-fx-background-color: #f39c12; -fx-text-fill: white;";
                            break;
                        case ANNULEE:
                            style += "-fx-background-color: #e74c3c; -fx-text-fill: white;";
                            break;
                        case TERMINEE:
                            style += "-fx-background-color: #95a5a6; -fx-text-fill: white;";
                            break;
                    }
                    setStyle(style);
                }
            }
        });

        // Colonne actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button detailsBtn = new Button("👁️");
            private final Button annulerBtn = new Button("❌");
            private final Button evaluerBtn = new Button("⭐");
            private final Button pdfBtn = new Button("📄");
            private final Button payerBtn = new Button("💳");
            private final HBox box = new HBox(5, detailsBtn, annulerBtn, evaluerBtn, pdfBtn, payerBtn);
            {
                detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                detailsBtn.setTooltip(new Tooltip("Voir les détails"));

                annulerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                annulerBtn.setTooltip(new Tooltip("Annuler la réservation"));

                evaluerBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                evaluerBtn.setTooltip(new Tooltip("Évaluer la chambre"));

                pdfBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
                pdfBtn.setTooltip(new Tooltip("Exporter en PDF"));

                detailsBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    showDetailsDialog(reservation);
                });

                annulerBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleAnnuler(reservation);
                });

                evaluerBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    showEvaluationDialog(reservation);
                });

                pdfBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleExportPdf(reservation);
                });
                payerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                payerBtn.setTooltip(new Tooltip("Payer la réservation"));

                payerBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    ouvrirPaiement(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());

                    boolean peutAnnuler = reservation.getStatut() == StatutReservation.EN_ATTENTE ||
                            reservation.getStatut() == StatutReservation.CONFIRMEE;

                    annulerBtn.setVisible(peutAnnuler);
                    annulerBtn.setManaged(peutAnnuler);

                    boolean peutEvaluer = reservation.getStatut() == StatutReservation.TERMINEE;

                    evaluerBtn.setVisible(peutEvaluer);
                    evaluerBtn.setManaged(peutEvaluer);

                    // Le PDF est toujours visible
                    pdfBtn.setVisible(true);
                    pdfBtn.setManaged(true);

                    setGraphic(box);
                }
            }
        });
    }

    private void loadReservations() {
        User currentUser = SessionManager.getCurrentUser();
        toutesLesReservations = reservationService.obtenirReservationsParUtilisateur(currentUser.getId());
        reservationsTableView.getItems().setAll(toutesLesReservations);
        updateStats();
    }
    private void ouvrirPaiement(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Paiement.fxml")
            );

            Parent root = loader.load();

            // Récupérer controller
            PaiementController controller = loader.getController();

            // Injecter données
            controller.setPaiementData(
                    reservation.getId(),
                    reservation.getPrixTotal()
            );

            Stage stage = new Stage();
            stage.setTitle("Paiement réservation #" + reservation.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Après paiement → refresh
            handleRefresh();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Impossible d'ouvrir la fenêtre de paiement.");
        }
    }
    @FXML
    private void handleFilter() {
        String statutFiltre = statutFilterComboBox.getValue();

        if (statutFiltre.equals("Tous")) {
            reservationsTableView.getItems().setAll(toutesLesReservations);
        } else {
            List<Reservation> filtrees = toutesLesReservations.stream()
                    .filter(r -> r.getStatut().name().equals(statutFiltre))
                    .collect(Collectors.toList());
            reservationsTableView.getItems().setAll(filtrees);
        }
    }

    @FXML
    private void handleRefresh() {
        loadReservations();
        statutFilterComboBox.setValue("Tous");
    }

    private void handleAnnuler(Reservation reservation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler la réservation #" + reservation.getId());
        confirm.setContentText("Êtes-vous sûr de vouloir annuler cette réservation ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (reservationService.annulerReservation(reservation.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation annulée!");
                handleRefresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler la réservation.");
            }
        }
    }

    private void showDetailsDialog(Reservation reservation) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détails de la réservation #" + reservation.getId());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Récupérer la chambre
        Chambre chambre = chambreService.obtenirChambreParId(reservation.getChambreId());

        // Informations
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

        Label titleLabel = new Label("📋 Informations de la réservation");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        String info = String.format(
                "Référence: #%d\n" +
                        "Statut: %s\n" +
                        "Date de réservation: %s\n\n" +
                        "Chambre: %s - %s\n" +
                        "Étage: %d\n\n" +
                        "Date d'arrivée: %s\n" +
                        "Date de départ: %s\n" +
                        "Nombre de nuits: %d\n" +
                        "Nombre de personnes: %d\n\n" +
                        "Prix par nuit: %.2f DT\n" +
                        "Prix total: %.2f DT",
                reservation.getId(),
                reservation.getStatut(),
                reservation.getDateReservation().toLocalDate(),
                chambre.getNumero(),
                chambre.getType(),
                chambre.getEtage(),
                reservation.getDateCheckin(),
                reservation.getDateCheckout(),
                reservation.getNombreNuits(),
                reservation.getNombrePersonnes(),
                chambre.getPrixParNuit(),
                reservation.getPrixTotal()
        );

        TextArea infoArea = new TextArea(info);
        infoArea.setEditable(false);
        infoArea.setPrefRowCount(15);
        infoArea.setStyle("-fx-font-size: 13px;");

        infoBox.getChildren().addAll(titleLabel, new Separator(), infoArea);

        // Commentaire
        if (reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty()) {
            VBox commentBox = new VBox(10);
            commentBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

            Label commentLabel = new Label("💬 Commentaire");
            commentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            TextArea commentArea = new TextArea(reservation.getCommentaire());
            commentArea.setEditable(false);
            commentArea.setPrefRowCount(3);

            commentBox.getChildren().addAll(commentLabel, new Separator(), commentArea);
            root.getChildren().add(commentBox);
        }

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20;");
        closeBtn.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setStyle("-fx-alignment: center;");

        root.getChildren().addAll(infoBox, buttonBox);

        Scene scene = new Scene(root, 500, 600);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showEvaluationDialog(Reservation reservation) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Évaluer la chambre");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        Chambre chambre = chambreService.obtenirChambreParId(reservation.getChambreId());

        Label titleLabel = new Label("⭐ Évaluer votre séjour");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label chambreLabel = new Label("Chambre: " + chambre.getNumero() + " - " + chambre.getType());
        chambreLabel.setStyle("-fx-font-size: 14px;");

        // Note
        VBox noteBox = new VBox(10);
        noteBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

        Label noteLabel = new Label("Note (1 à 5 étoiles):");
        noteLabel.setStyle("-fx-font-weight: bold;");

        HBox starsBox = new HBox(10);
        ToggleGroup starsGroup = new ToggleGroup();

        for (int i = 1; i <= 5; i++) {
            RadioButton star = new RadioButton(i + " ⭐");
            star.setToggleGroup(starsGroup);
            star.setUserData(i);
            if (i == 5) star.setSelected(true);
            starsBox.getChildren().add(star);
        }

        noteBox.getChildren().addAll(noteLabel, starsBox);

        // Commentaire
        VBox commentBox = new VBox(10);
        commentBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

        Label commentLabel = new Label("Votre avis:");
        commentLabel.setStyle("-fx-font-weight: bold;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Partagez votre expérience...");
        commentArea.setPrefRowCount(5);

        commentBox.getChildren().addAll(commentLabel, commentArea);

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setStyle("-fx-alignment: center;");

        Button submitBtn = new Button("✅ Soumettre l'évaluation");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 20;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 20;");

        submitBtn.setOnAction(e -> {
            int note = (int) starsGroup.getSelectedToggle().getUserData();
            String commentaire = commentArea.getText().trim();

            if (commentaire.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention",
                        "Veuillez ajouter un commentaire!");
                return;
            }

            User currentUser = SessionManager.getCurrentUser();
            Evaluation evaluation = new Evaluation(
                    reservation.getId(),
                    chambre.getId(),
                    currentUser.getId(),
                    note,
                    commentaire
            );

            if (evaluationService.ajouterEvaluation(evaluation)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Merci pour votre évaluation!");
                dialog.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'enregistrer l'évaluation.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        buttonsBox.getChildren().addAll(submitBtn, cancelBtn);

        root.getChildren().addAll(titleLabel, chambreLabel, noteBox, commentBox, buttonsBox);

        Scene scene = new Scene(root, 500, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateStats() {
        statsLabel.setText(toutesLesReservations.size() + " réservation(s)");
    }

    private void handleExportPdf(Reservation reservation) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        String fileName = "Reservation_" + reservation.getId() + "_" +
                LocalDate.now().toString() + ".pdf";
        fileChooser.setInitialFileName(fileName);

        java.io.File file = fileChooser.showSaveDialog(reservationsTableView.getScene().getWindow());
        if (file != null) {
            User user = SessionManager.getCurrentUser();
            Chambre chambre = chambreService.obtenirChambreParId(reservation.getChambreId());

            if (pdfService.genererFacture(reservation, user, chambre, file.getAbsolutePath())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Facture PDF générée avec succès !\n" + file.getAbsolutePath());
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de générer le PDF.");
            }
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