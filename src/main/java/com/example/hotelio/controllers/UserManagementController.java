package com.example.hotelio.controllers;

import com.example.hotelio.entities.*;
import com.example.hotelio.services.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserManagementController {

    @FXML private TableView<User> tableView;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> prenomColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> telephoneColumn;
    @FXML private TableColumn<User, String> typeColumn;
    @FXML private TableColumn<User, Boolean> actifColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private Button refreshButton;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        // Pas besoin de refaire PropertyValueFactory ici, déjà dans FXML
        // Mais on configure la colonne des actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user);
                });

                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
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
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        tableView.getItems().setAll(userService.obtenirTousLesUtilisateurs());
    }

    private void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cet utilisateur ?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (userService.supprimerUtilisateur(user.getId())) {
                    loadData();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'utilisateur.");
                }
            }
        });
    }

    private void showEditUserDialog(User user) {
        Stage dialog = new Stage();
        dialog.setTitle("Modifier utilisateur");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField nomField = new TextField(user.getNom());
        TextField prenomField = new TextField(user.getPrenom());
        TextField emailField = new TextField(user.getEmail());
        TextField telField = new TextField(user.getTelephone());

        CheckBox actifBox = new CheckBox("Actif");
        actifBox.setSelected(user.isActif());

        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        saveBtn.setOnAction(e -> {
            user.setNom(nomField.getText());
            user.setPrenom(prenomField.getText());
            user.setEmail(emailField.getText());
            user.setTelephone(telField.getText());
            user.setActif(actifBox.isSelected());

            if (userService.modifierUtilisateur(user)) {
                loadData();
                dialog.close();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur modifié!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier l'utilisateur.");
            }
        });

        root.getChildren().addAll(
                new Label("Nom"), nomField,
                new Label("Prénom"), prenomField,
                new Label("Email"), emailField,
                new Label("Téléphone"), telField,
                actifBox,
                saveBtn
        );

        Scene scene = new Scene(root, 400, 400);
        dialog.setScene(scene);
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