package com.example.hotelio.controllers;

import com.example.hotelio.entities.Chambre;
import com.example.hotelio.enums.StatutChambre;
import com.example.hotelio.enums.TypeChambre;
import com.example.hotelio.services.ChambreService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChambreManagementController {

    @FXML private TableView<Chambre> tableView;
    @FXML private TableColumn<Chambre, Integer> idColumn;
    @FXML private TableColumn<Chambre, String> numeroColumn;
    @FXML private TableColumn<Chambre, TypeChambre> typeColumn;
    @FXML private TableColumn<Chambre, Double> prixColumn;
    @FXML private TableColumn<Chambre, Integer> capaciteColumn;
    @FXML private TableColumn<Chambre, StatutChambre> statutColumn;
    @FXML private TableColumn<Chambre, Integer> etageColumn;
    @FXML private TableColumn<Chambre, Void> actionsColumn;
    @FXML private Button addButton;
    @FXML private Button refreshButton;

    private ChambreService chambreService = new ChambreService();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(e -> {
                    Chambre chambre = getTableView().getItems().get(getIndex());
                    showEditChambreDialog(chambre);
                });

                deleteBtn.setOnAction(e -> {
                    Chambre chambre = getTableView().getItems().get(getIndex());
                    handleDeleteChambre(chambre);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void handleAdd() {
        showAddChambreDialog();
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        tableView.getItems().setAll(chambreService.obtenirToutesLesChambres());
    }

    private void handleDeleteChambre(Chambre chambre) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cette chambre ?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (chambreService.supprimerChambre(chambre.getId())) {
                    loadData();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Chambre supprimée!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la chambre.");
                }
            }
        });
    }

    private void showAddChambreDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Ajouter une chambre");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField numField = new TextField();
        TextField prixField = new TextField();
        TextField capField = new TextField();
        TextField etageField = new TextField();
        TextArea descArea = new TextArea();

        ComboBox<TypeChambre> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(TypeChambre.values());

        ComboBox<StatutChambre> statutBox = new ComboBox<>();
        statutBox.getItems().addAll(StatutChambre.values());
        statutBox.setValue(StatutChambre.DISPONIBLE);

        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        saveBtn.setOnAction(e -> {
            try {
                Chambre chambre = new Chambre();
                chambre.setNumero(numField.getText());
                chambre.setType(typeBox.getValue());
                chambre.setPrixParNuit(Double.parseDouble(prixField.getText()));
                chambre.setCapacite(Integer.parseInt(capField.getText()));
                chambre.setDescription(descArea.getText());
                chambre.setStatut(statutBox.getValue());
                chambre.setEtage(Integer.parseInt(etageField.getText()));

                if (chambreService.ajouterChambre(chambre)) {
                    loadData();
                    dialog.close();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Chambre ajoutée!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter la chambre.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides.");
            }
        });

        root.getChildren().addAll(
                new Label("Numéro"), numField,
                new Label("Type"), typeBox,
                new Label("Prix / nuit"), prixField,
                new Label("Capacité"), capField,
                new Label("Étage"), etageField,
                new Label("Description"), descArea,
                new Label("Statut"), statutBox,
                saveBtn
        );

        dialog.setScene(new Scene(root, 400, 550));
        dialog.show();
    }

    private void showEditChambreDialog(Chambre chambre) {
        Stage dialog = new Stage();
        dialog.setTitle("Modifier chambre");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField numField = new TextField(chambre.getNumero());
        TextField prixField = new TextField(String.valueOf(chambre.getPrixParNuit()));
        TextField capField = new TextField(String.valueOf(chambre.getCapacite()));
        TextField etageField = new TextField(String.valueOf(chambre.getEtage()));
        TextArea descArea = new TextArea(chambre.getDescription());

        ComboBox<TypeChambre> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(TypeChambre.values());
        typeBox.setValue(chambre.getType());

        ComboBox<StatutChambre> statutBox = new ComboBox<>();
        statutBox.getItems().addAll(StatutChambre.values());
        statutBox.setValue(chambre.getStatut());

        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        saveBtn.setOnAction(e -> {
            try {
                chambre.setNumero(numField.getText());
                chambre.setType(typeBox.getValue());
                chambre.setPrixParNuit(Double.parseDouble(prixField.getText()));
                chambre.setCapacite(Integer.parseInt(capField.getText()));
                chambre.setDescription(descArea.getText());
                chambre.setStatut(statutBox.getValue());
                chambre.setEtage(Integer.parseInt(etageField.getText()));

                if (chambreService.modifierChambre(chambre)) {
                    loadData();
                    dialog.close();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Chambre modifiée!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la chambre.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides.");
            }
        });

        root.getChildren().addAll(
                new Label("Numéro"), numField,
                new Label("Type"), typeBox,
                new Label("Prix / nuit"), prixField,
                new Label("Capacité"), capField,
                new Label("Étage"), etageField,
                new Label("Description"), descArea,
                new Label("Statut"), statutBox,
                saveBtn
        );

        dialog.setScene(new Scene(root, 400, 550));
        dialog.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}