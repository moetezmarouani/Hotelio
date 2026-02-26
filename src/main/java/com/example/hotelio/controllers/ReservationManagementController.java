package com.example.hotelio.controllers;

import com.example.hotelio.entities.*;
import com.example.hotelio.enums.StatutReservation;
import com.example.hotelio.services.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReservationManagementController {

    @FXML private Label totalLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label confirmeesLabel;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private ComboBox<String> periodeComboBox;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private TableView<Reservation> tableView;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> clientColumn;
    @FXML private TableColumn<Reservation, String> emailColumn;
    @FXML private TableColumn<Reservation, String> chambreColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkinColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkoutColumn;
    @FXML private TableColumn<Reservation, Integer> personnesColumn;
    @FXML private TableColumn<Reservation, Double> prixColumn;
    @FXML private TableColumn<Reservation, StatutReservation> statutColumn;
    @FXML private TableColumn<Reservation, Void> actionsColumn;

    private ReservationService reservationService = new ReservationService();
    private UserService userService = new UserService();
    private ChambreService chambreService = new ChambreService();

    private List<Reservation> toutesLesReservations;
    private List<Reservation> reservationsFiltrees;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTableColumns();
        loadReservations();
    }

    private void setupComboBoxes() {
        // Statuts
        statutComboBox.getItems().add("Tous");
        for (StatutReservation statut : StatutReservation.values()) {
            statutComboBox.getItems().add(statut.name());
        }
        statutComboBox.setValue("Tous");

        // Périodes
        periodeComboBox.getItems().addAll(
                "Toutes",
                "Aujourd'hui",
                "Cette semaine",
                "Ce mois",
                "À venir",
                "Passées"
        );
        periodeComboBox.setValue("Toutes");
    }

    private void setupTableColumns() {
        // Colonne client
        clientColumn.setCellValueFactory(cellData -> {
            User user = userService.obtenirUtilisateurParId(cellData.getValue().getUserId());
            return new javafx.beans.property.SimpleStringProperty(
                    user != null ? user.getNomComplet() : "Inconnu"
            );
        });

        // Colonne email
        emailColumn.setCellValueFactory(cellData -> {
            User user = userService.obtenirUtilisateurParId(cellData.getValue().getUserId());
            return new javafx.beans.property.SimpleStringProperty(
                    user != null ? user.getEmail() : "N/A"
            );
        });

        // Colonne chambre
        chambreColumn.setCellValueFactory(cellData -> {
            Chambre chambre = chambreService.obtenirChambreParId(cellData.getValue().getChambreId());
            return new javafx.beans.property.SimpleStringProperty(
                    chambre != null ? chambre.getNumero() + " - " + chambre.getType() : "N/A"
            );
        });

        // Format prix
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

        // Statut avec couleur
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

        // Actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button detailsBtn = new Button("👁️");
            private final Button confirmerBtn = new Button("✅");
            private final Button annulerBtn = new Button("❌");
            private final HBox box = new HBox(5, detailsBtn, confirmerBtn, annulerBtn);

            {
                detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                detailsBtn.setTooltip(new Tooltip("Détails"));

                confirmerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                confirmerBtn.setTooltip(new Tooltip("Confirmer"));

                annulerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                annulerBtn.setTooltip(new Tooltip("Annuler"));

                detailsBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    showDetailsDialog(reservation);
                });

                confirmerBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleConfirmer(reservation);
                });

                annulerBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleAnnuler(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());

                    // Montrer confirmer seulement pour EN_ATTENTE
                    confirmerBtn.setVisible(reservation.getStatut() == StatutReservation.EN_ATTENTE);
                    confirmerBtn.setManaged(reservation.getStatut() == StatutReservation.EN_ATTENTE);

                    // Montrer annuler seulement pour EN_ATTENTE et CONFIRMEE
                    boolean peutAnnuler = reservation.getStatut() == StatutReservation.EN_ATTENTE ||
                            reservation.getStatut() == StatutReservation.CONFIRMEE;
                    annulerBtn.setVisible(peutAnnuler);
                    annulerBtn.setManaged(peutAnnuler);

                    setGraphic(box);
                }
            }
        });
    }

    private void loadReservations() {
        toutesLesReservations = reservationService.obtenirToutesLesReservations();
        reservationsFiltrees = toutesLesReservations;
        tableView.getItems().setAll(toutesLesReservations);
        updateStatistics();
    }

    @FXML
    private void handleFilter() {
        String statut = statutComboBox.getValue();
        String periode = periodeComboBox.getValue();

        reservationsFiltrees = toutesLesReservations.stream()
                .filter(r -> {
                    // Filtre par statut
                    if (!statut.equals("Tous") && !r.getStatut().name().equals(statut)) {
                        return false;
                    }

                    // Filtre par période
                    LocalDate now = LocalDate.now();
                    switch (periode) {
                        case "Aujourd'hui":
                            return r.getDateCheckin().equals(now) || r.getDateCheckout().equals(now);
                        case "Cette semaine":
                            LocalDate debutSemaine = now.minusDays(now.getDayOfWeek().getValue() - 1);
                            LocalDate finSemaine = debutSemaine.plusDays(6);
                            return !r.getDateCheckin().isAfter(finSemaine) &&
                                    !r.getDateCheckout().isBefore(debutSemaine);
                        case "Ce mois":
                            return r.getDateCheckin().getMonth() == now.getMonth() &&
                                    r.getDateCheckin().getYear() == now.getYear();
                        case "À venir":
                            return r.getDateCheckin().isAfter(now);
                        case "Passées":
                            return r.getDateCheckout().isBefore(now);
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());

        tableView.getItems().setAll(reservationsFiltrees);
        handleSearch(); // Appliquer aussi la recherche textuelle
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            tableView.getItems().setAll(reservationsFiltrees);
            return;
        }

        List<Reservation> resultats = reservationsFiltrees.stream()
                .filter(r -> {
                    User user = userService.obtenirUtilisateurParId(r.getUserId());
                    if (user != null) {
                        return user.getNom().toLowerCase().contains(searchText) ||
                                user.getPrenom().toLowerCase().contains(searchText) ||
                                user.getEmail().toLowerCase().contains(searchText);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        tableView.getItems().setAll(resultats);
    }

    @FXML
    private void handleRefresh() {
        loadReservations();
        statutComboBox.setValue("Tous");
        periodeComboBox.setValue("Toutes");
        searchField.clear();
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les réservations");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("reservations_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // En-têtes
                writer.write("ID,Client,Email,Chambre,Arrivée,Départ,Personnes,Prix Total,Statut\n");

                // Données
                for (Reservation r : tableView.getItems()) {
                    User user = userService.obtenirUtilisateurParId(r.getUserId());
                    Chambre chambre = chambreService.obtenirChambreParId(r.getChambreId());

                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%d,%.2f,%s\n",
                            r.getId(),
                            user != null ? user.getNomComplet() : "N/A",
                            user != null ? user.getEmail() : "N/A",
                            chambre != null ? chambre.getNumero() : "N/A",
                            r.getDateCheckin(),
                            r.getDateCheckout(),
                            r.getNombrePersonnes(),
                            r.getPrixTotal(),
                            r.getStatut()
                    ));
                }

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Données exportées avec succès!");

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de l'export: " + e.getMessage());
            }
        }
    }

    private void handleConfirmer(Reservation reservation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Confirmer la réservation #" + reservation.getId());
        confirm.setContentText("Confirmer cette réservation ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (reservationService.confirmerReservation(reservation.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation confirmée!");
                loadReservations();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de confirmer.");
            }
        }
    }

    private void handleAnnuler(Reservation reservation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler la réservation #" + reservation.getId());
        confirm.setContentText("Annuler cette réservation ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (reservationService.annulerReservation(reservation.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation annulée!");
                loadReservations();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler.");
            }
        }
    }

    private void showDetailsDialog(Reservation reservation) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détails de la réservation #" + reservation.getId());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        User user = userService.obtenirUtilisateurParId(reservation.getUserId());
        Chambre chambre = chambreService.obtenirChambreParId(reservation.getChambreId());

        String details = String.format(
                "═══════════════════════════════════\n" +
                        "RÉSERVATION #%d\n" +
                        "═══════════════════════════════════\n\n" +
                        "📋 INFORMATIONS CLIENT\n" +
                        "   Nom: %s\n" +
                        "   Email: %s\n" +
                        "   Téléphone: %s\n\n" +
                        "🛏️  INFORMATIONS CHAMBRE\n" +
                        "   Numéro: %s\n" +
                        "   Type: %s\n" +
                        "   Étage: %d\n" +
                        "   Prix/nuit: %.2f DT\n\n" +
                        "📅 DATES DU SÉJOUR\n" +
                        "   Arrivée: %s\n" +
                        "   Départ: %s\n" +
                        "   Nuits: %d\n" +
                        "   Personnes: %d\n\n" +
                        "💰 FACTURATION\n" +
                        "   Prix total: %.2f DT\n" +
                        "   Statut: %s\n\n" +
                        "📝 Date de réservation: %s\n",
                reservation.getId(),
                user != null ? user.getNomComplet() : "N/A",
                user != null ? user.getEmail() : "N/A",
                user != null ? user.getTelephone() : "N/A",
                chambre != null ? chambre.getNumero() : "N/A",
                chambre != null ? chambre.getType().toString() : "N/A",
                chambre != null ? chambre.getEtage() : 0,
                chambre != null ? chambre.getPrixParNuit() : 0.0,
                reservation.getDateCheckin(),
                reservation.getDateCheckout(),
                reservation.getNombreNuits(),
                reservation.getNombrePersonnes(),
                reservation.getPrixTotal(),
                reservation.getStatut(),
                reservation.getDateReservation().toLocalDate()
        );

        if (reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty()) {
            details += "\n💬 COMMENTAIRE\n   " + reservation.getCommentaire();
        }

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(textArea, closeBtn);

        Scene scene = new Scene(root, 600, 650);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateStatistics() {
        totalLabel.setText(String.valueOf(toutesLesReservations.size()));

        long enAttente = toutesLesReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                .count();
        enAttenteLabel.setText(String.valueOf(enAttente));

        long confirmees = toutesLesReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .count();
        confirmeesLabel.setText(String.valueOf(confirmees));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}