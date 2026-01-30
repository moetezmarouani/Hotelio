package com.example.hotelio.controllers;

import com.example.hotelio.entities.*;
import com.example.hotelio.enums.TypeChambre;
import com.example.hotelio.services.ChambreService;
import com.example.hotelio.services.ReservationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RechercheChambresController {

    @FXML private DatePicker dateCheckinPicker;
    @FXML private DatePicker dateCheckoutPicker;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private Spinner<Integer> capaciteSpinner;
    @FXML private TextField prixMaxField;
    @FXML private ComboBox<String> etageComboBox;
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Label resultLabel;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private TableView<Chambre> chambresTableView;
    @FXML private TableColumn<Chambre, String> numeroColumn;
    @FXML private TableColumn<Chambre, TypeChambre> typeColumn;
    @FXML private TableColumn<Chambre, Integer> capaciteColumn;
    @FXML private TableColumn<Chambre, Integer> etageColumn;
    @FXML private TableColumn<Chambre, Double> prixColumn;
    @FXML private TableColumn<Chambre, String> descriptionColumn;
    @FXML private TableColumn<Chambre, Void> actionsColumn;

    private ChambreService chambreService = new ChambreService();
    private ReservationService reservationService = new ReservationService();
    private ObservableList<Chambre> toutesLesChambres = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupSpinner();
        setupTableColumns();
        setupSortOptions();

        // Dates par défaut
        dateCheckinPicker.setValue(LocalDate.now());
        dateCheckoutPicker.setValue(LocalDate.now().plusDays(1));

        // Charger toutes les chambres au démarrage
        loadAllChambres();
    }

    private void setupComboBoxes() {
        // Types de chambre
        typeComboBox.getItems().add("Tous");
        for (TypeChambre type : TypeChambre.values()) {
            typeComboBox.getItems().add(type.name());
        }
        typeComboBox.setValue("Tous");

        // Étages
        etageComboBox.getItems().addAll("Tous", "1", "2", "3", "4");
        etageComboBox.setValue("Tous");
    }

    private void setupSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        capaciteSpinner.setValueFactory(valueFactory);
    }

    private void setupTableColumns() {
        // Configuration de la colonne actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button reserverBtn = new Button("📅 Réserver");

            {
                reserverBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 5 15;");

                reserverBtn.setOnAction(e -> {
                    Chambre chambre = getTableView().getItems().get(getIndex());
                    showReservationDialog(chambre);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : reserverBtn);
            }
        });

        // Format de la colonne prix
        prixColumn.setCellFactory(col -> new TableCell<Chambre, Double>() {
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
    }

    private void setupSortOptions() {
        sortComboBox.getItems().addAll(
                "Prix croissant",
                "Prix décroissant",
                "Capacité croissante",
                "Capacité décroissante",
                "Numéro de chambre"
        );
        sortComboBox.setValue("Prix croissant");
    }

    private void loadAllChambres() {
        List<Chambre> chambres = chambreService.obtenirToutesLesChambres()
                .stream()
                .filter(c -> c.getStatut().name().equals("DISPONIBLE"))
                .collect(Collectors.toList());

        toutesLesChambres.setAll(chambres);
        chambresTableView.setItems(toutesLesChambres);
        updateResultLabel(chambres.size());
    }

    @FXML
    private void handleSearch() {
        LocalDate checkin = dateCheckinPicker.getValue();
        LocalDate checkout = dateCheckoutPicker.getValue();

        // Validation des dates
        if (checkin == null || checkout == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner les dates d'arrivée et de départ!");
            return;
        }

        if (!checkout.isAfter(checkin)) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "La date de départ doit être après la date d'arrivée!");
            return;
        }

        if (checkin.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "La date d'arrivée ne peut pas être dans le passé!");
            return;
        }

        // Recherche des chambres disponibles
        List<Chambre> chambresDisponibles = chambreService.obtenirChambresDisponibles(checkin, checkout);

        // Appliquer les filtres supplémentaires
        chambresDisponibles = appliquerFiltres(chambresDisponibles);

        toutesLesChambres.setAll(chambresDisponibles);
        chambresTableView.setItems(toutesLesChambres);
        updateResultLabel(chambresDisponibles.size());

        // Appliquer le tri
        handleSort();
    }

    private List<Chambre> appliquerFiltres(List<Chambre> chambres) {
        return chambres.stream()
                .filter(c -> {
                    // Filtre par type
                    String typeSelectionne = typeComboBox.getValue();
                    if (!typeSelectionne.equals("Tous") && !c.getType().name().equals(typeSelectionne)) {
                        return false;
                    }

                    // Filtre par capacité
                    if (c.getCapacite() < capaciteSpinner.getValue()) {
                        return false;
                    }

                    // Filtre par prix max
                    String prixMaxStr = prixMaxField.getText().trim();
                    if (!prixMaxStr.isEmpty()) {
                        try {
                            double prixMax = Double.parseDouble(prixMaxStr);
                            if (c.getPrixParNuit() > prixMax) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            // Ignorer si le prix n'est pas valide
                        }
                    }

                    // Filtre par étage
                    String etageSelectionne = etageComboBox.getValue();
                    if (!etageSelectionne.equals("Tous") && c.getEtage() != Integer.parseInt(etageSelectionne)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    @FXML
    private void handleSort() {
        String sortOption = sortComboBox.getValue();
        if (sortOption == null) return;

        ObservableList<Chambre> items = chambresTableView.getItems();
        List<Chambre> sortedList = items.stream().sorted((c1, c2) -> {
            switch (sortOption) {
                case "Prix croissant":
                    return Double.compare(c1.getPrixParNuit(), c2.getPrixParNuit());
                case "Prix décroissant":
                    return Double.compare(c2.getPrixParNuit(), c1.getPrixParNuit());
                case "Capacité croissante":
                    return Integer.compare(c1.getCapacite(), c2.getCapacite());
                case "Capacité décroissante":
                    return Integer.compare(c2.getCapacite(), c1.getCapacite());
                case "Numéro de chambre":
                    return c1.getNumero().compareTo(c2.getNumero());
                default:
                    return 0;
            }
        }).collect(Collectors.toList());

        chambresTableView.getItems().setAll(sortedList);
    }

    @FXML
    private void handleReset() {
        dateCheckinPicker.setValue(LocalDate.now());
        dateCheckoutPicker.setValue(LocalDate.now().plusDays(1));
        typeComboBox.setValue("Tous");
        capaciteSpinner.getValueFactory().setValue(1);
        prixMaxField.clear();
        etageComboBox.setValue("Tous");

        loadAllChambres();
    }

    private void showReservationDialog(Chambre chambre) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Réserver la chambre " + chambre.getNumero());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Informations de la chambre
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

        Label titleLabel = new Label("📋 Détails de la réservation");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label chambreInfo = new Label(
                "Chambre: " + chambre.getNumero() + " - " + chambre.getType() + "\n" +
                        "Capacité: " + chambre.getCapacite() + " personne(s)\n" +
                        "Prix par nuit: " + String.format("%.2f DT", chambre.getPrixParNuit())
        );
        chambreInfo.setStyle("-fx-font-size: 14px;");

        infoBox.getChildren().addAll(titleLabel, new Separator(), chambreInfo);

        // Dates
        VBox datesBox = new VBox(10);
        datesBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

        Label datesLabel = new Label("📅 Dates du séjour");
        datesLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        DatePicker checkinPicker = new DatePicker(dateCheckinPicker.getValue());
        checkinPicker.setPromptText("Date d'arrivée");
        checkinPicker.setMaxWidth(Double.MAX_VALUE);

        DatePicker checkoutPicker = new DatePicker(dateCheckoutPicker.getValue());
        checkoutPicker.setPromptText("Date de départ");
        checkoutPicker.setMaxWidth(Double.MAX_VALUE);

        Label nuitLabel = new Label("Nombre de nuits: 0");
        nuitLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label prixTotalLabel = new Label("Prix total: 0.00 DT");
        prixTotalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        // Calcul automatique
        Runnable calculer = () -> {
            if (checkinPicker.getValue() != null && checkoutPicker.getValue() != null) {
                LocalDate cin = checkinPicker.getValue();
                LocalDate cout = checkoutPicker.getValue();
                if (cout.isAfter(cin)) {
                    long nuits = java.time.temporal.ChronoUnit.DAYS.between(cin, cout);
                    double total = nuits * chambre.getPrixParNuit();
                    nuitLabel.setText("Nombre de nuits: " + nuits);
                    prixTotalLabel.setText(String.format("Prix total: %.2f DT", total));
                }
            }
        };

        checkinPicker.setOnAction(e -> calculer.run());
        checkoutPicker.setOnAction(e -> calculer.run());
        calculer.run();

        datesBox.getChildren().addAll(datesLabel, new Separator(),
                new Label("Date d'arrivée:"), checkinPicker,
                new Label("Date de départ:"), checkoutPicker,
                nuitLabel, prixTotalLabel
        );

        // Nombre de personnes
        VBox personnesBox = new VBox(10);
        personnesBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

        Label personnesLabel = new Label("👥 Nombre de personnes");
        personnesLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Spinner<Integer> personnesSpinner = new Spinner<>(1, chambre.getCapacite(), 1);
        personnesSpinner.setMaxWidth(Double.MAX_VALUE);

        personnesBox.getChildren().addAll(personnesLabel, new Separator(), personnesSpinner);

        // Commentaire
        VBox commentBox = new VBox(10);
        commentBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

        Label commentLabel = new Label("💬 Commentaire (optionnel)");
        commentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Demandes spéciales, allergies, etc.");
        commentArea.setPrefRowCount(3);

        commentBox.getChildren().addAll(commentLabel, new Separator(), commentArea);

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setStyle("-fx-alignment: center;");

        Button confirmerBtn = new Button("✅ Confirmer la réservation");
        confirmerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");

        Button annulerBtn = new Button("❌ Annuler");
        annulerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");

        confirmerBtn.setOnAction(e -> {
            LocalDate cin = checkinPicker.getValue();
            LocalDate cout = checkoutPicker.getValue();

            if (cin == null || cout == null || !cout.isAfter(cin)) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Dates invalides!");
                return;
            }

            // Créer la réservation
            User currentUser = SessionManager.getCurrentUser();
            long nuits = java.time.temporal.ChronoUnit.DAYS.between(cin, cout);
            double total = nuits * chambre.getPrixParNuit();

            Reservation reservation = new Reservation(
                    currentUser.getId(),
                    chambre.getId(),
                    cin,
                    cout,
                    personnesSpinner.getValue(),
                    total
            );
            reservation.setCommentaire(commentArea.getText());

            if (reservationService.creerReservation(reservation)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Réservation créée avec succès!\n\n" +
                                "Référence: #" + reservation.getId() + "\n" +
                                "Montant total: " + String.format("%.2f DT", total));
                dialog.close();
                handleSearch(); // Rafraîchir la liste
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de créer la réservation.");
            }
        });

        annulerBtn.setOnAction(e -> dialog.close());

        buttonsBox.getChildren().addAll(confirmerBtn, annulerBtn);

        root.getChildren().addAll(infoBox, datesBox, personnesBox, commentBox, buttonsBox);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(scrollPane, 500, 700);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateResultLabel(int count) {
        resultLabel.setText(count + " chambre(s) disponible(s)");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}