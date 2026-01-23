package com.example.hotelio;


import com.example.hotelio.entities.Chambre;
import com.example.hotelio.entities.User;
import com.example.hotelio.enums.Role;
import com.example.hotelio.enums.StatutChambre;
import com.example.hotelio.enums.TypeChambre;
import com.example.hotelio.services.ChambreService;
import com.example.hotelio.services.UserService;
import com.example.hotelio.utils.DatabaseConnection;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// ============== Main Application ==============

public class HotelIOApp extends Application {

    private static User utilisateurConnecte;

    @Override
    public void start(Stage primaryStage) {
        // Test de connexion à la base de données
        if (!DatabaseConnection.testConnection()) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de se connecter à la base de données!");
            return;
        }

        showLoginScreen(primaryStage);
    }

    // Écran de connexion
    private void showLoginScreen(Stage stage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");

        // Titre
        Label titleLabel = new Label("🏨 HotelIO");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Système de Gestion Hôtelière");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ecf0f1;");

        // Formulaire de connexion
        VBox loginBox = new VBox(15);
        loginBox.setMaxWidth(400);
        loginBox.setPadding(new Insets(30));
        loginBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label loginTitle = new Label("Connexion");
        loginTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        Button loginButton = new Button("Se connecter");
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12; -fx-cursor: hand;");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Button registerButton = new Button("Créer un compte");
        registerButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                "-fx-font-size: 12px; -fx-cursor: hand; -fx-underline: true;");

        // Action connexion
        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention",
                        "Veuillez remplir tous les champs!");
                return;
            }

            UserService userService = new UserService();
            User user = userService.authentifier(email, password);

            if (user != null) {
                utilisateurConnecte = user;
                if (user.getRole() == Role.ADMIN) {
                    showAdminDashboard(stage);
                } else {
                    showClientDashboard(stage);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Email ou mot de passe incorrect!");
            }
        });

        // Action inscription
        registerButton.setOnAction(e -> showRegistrationScreen(stage));

        loginBox.getChildren().addAll(loginTitle, emailField, passwordField,
                loginButton, registerButton);

        root.getChildren().addAll(titleLabel, subtitleLabel, loginBox);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("HotelIO - Connexion");
        stage.setScene(scene);
        stage.show();
    }

    // Écran d'inscription
    private void showRegistrationScreen(Stage stage) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");

        VBox registerBox = new VBox(15);
        registerBox.setMaxWidth(450);
        registerBox.setPadding(new Insets(30));
        registerBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label title = new Label("Créer un compte");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le mot de passe");

        Button registerButton = new Button("S'inscrire");
        registerButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12;");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("← Retour");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db;");
        backButton.setOnAction(e -> showLoginScreen(stage));

        registerButton.setOnAction(e -> {
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

            User newUser = new User(
                    nomField.getText().trim(),
                    prenomField.getText().trim(),
                    emailField.getText().trim(),
                    telephoneField.getText().trim(),
                    passwordField.getText().trim(),
                    Role.CLIENT
            );

            UserService userService = new UserService();
            if (userService.creerUtilisateur(newUser)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Compte créé avec succès! Vous pouvez maintenant vous connecter.");
                showLoginScreen(stage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la création du compte. L'email existe peut-être déjà.");
            }
        });

        registerBox.getChildren().addAll(title, nomField, prenomField, emailField,
                telephoneField, passwordField, confirmPasswordField,
                registerButton, backButton);

        root.getChildren().add(registerBox);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
    }

    // Dashboard Admin
    private void showAdminDashboard(Stage stage) {
        BorderPane root = new BorderPane();

        // Menu supérieur
        HBox topMenu = new HBox(10);
        topMenu.setPadding(new Insets(15));
        topMenu.setStyle("-fx-background-color: #2c3e50;");

        Label welcomeLabel = new Label("👋 Bienvenue, " + utilisateurConnecte.getNomComplet());
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Déconnexion");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> showLoginScreen(stage));

        topMenu.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        root.setTop(topMenu);

        // Menu latéral
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setStyle("-fx-background-color: #34495e;");
        sideMenu.setMinWidth(200);

        Button usersButton = createMenuButton("👥 Utilisateurs");
        Button chambresButton = createMenuButton("🛏️ Chambres");
        Button reservationsButton = createMenuButton("📅 Réservations");

        sideMenu.getChildren().addAll(usersButton, chambresButton, reservationsButton);
        root.setLeft(sideMenu);

        // Zone centrale
        StackPane centerPane = new StackPane();
        centerPane.setPadding(new Insets(20));

        Label dashboardLabel = new Label("Tableau de bord Administrateur");
        dashboardLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        centerPane.getChildren().add(dashboardLabel);

        root.setCenter(centerPane);

        // Actions des boutons (à compléter avec les vues appropriées)
        usersButton.setOnAction(e -> {
            // Afficher la gestion des utilisateurs
            centerPane.getChildren().clear();
            centerPane.getChildren().add(createUserManagementView());
        });

        chambresButton.setOnAction(e -> {
            // Afficher la gestion des chambres
            centerPane.getChildren().clear();
            centerPane.getChildren().add(createChambreManagementView());
        });

        reservationsButton.setOnAction(e -> {
            // Afficher les réservations
            centerPane.getChildren().clear();
            centerPane.getChildren().add(createReservationManagementView());
        });

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("HotelIO - Admin Dashboard");
        stage.setScene(scene);
    }

    // Dashboard Client
    private void showClientDashboard(Stage stage) {
        BorderPane root = new BorderPane();

        // Menu supérieur
        HBox topMenu = new HBox(10);
        topMenu.setPadding(new Insets(15));
        topMenu.setStyle("-fx-background-color: #16a085;");

        Label welcomeLabel = new Label("👋 Bienvenue, " + utilisateurConnecte.getNomComplet());
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Déconnexion");
        logoutButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> showLoginScreen(stage));

        topMenu.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        root.setTop(topMenu);

        // Menu latéral
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setStyle("-fx-background-color: #1abc9c;");
        sideMenu.setMinWidth(200);

        Button rechercheButton = createMenuButton("🔍 Rechercher");
        Button mesReservationsButton = createMenuButton("📋 Mes Réservations");
        Button profilButton = createMenuButton("👤 Mon Profil");

        sideMenu.getChildren().addAll(rechercheButton, mesReservationsButton, profilButton);
        root.setLeft(sideMenu);

        // Zone centrale
        StackPane centerPane = new StackPane();
        centerPane.setPadding(new Insets(20));

        Label dashboardLabel = new Label("Bienvenue sur HotelIO!");
        dashboardLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        centerPane.getChildren().add(dashboardLabel);

        root.setCenter(centerPane);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("HotelIO - Client Dashboard");
        stage.setScene(scene);
    }

    // Créer un bouton de menu
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10; -fx-cursor: hand; " +
                "-fx-alignment: center-left;");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-padding: 10; -fx-cursor: hand; " +
                        "-fx-alignment: center-left;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-padding: 10; -fx-cursor: hand; " +
                        "-fx-alignment: center-left;"));
        return button;
    }

    // Vues de gestion (à implémenter)
    private VBox createUserManagementView() {

        UserService userService = new UserService();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Gestion des Utilisateurs");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // ===== TableView =====
        TableView<User> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Colonnes
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<User, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> telCol = new TableColumn<>("Téléphone");
        telCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        TableColumn<User, Role> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<User, Boolean> actifCol = new TableColumn<>("Actif");
        actifCol.setCellValueFactory(new PropertyValueFactory<>("actif"));

        // ===== Actions =====
        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user, tableView);
                });

                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Supprimer cet utilisateur ?");

                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            userService.supprimerUtilisateur(user.getId());
                            tableView.getItems().setAll(userService.obtenirTousLesUtilisateurs());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableView.getColumns().addAll(
                idCol, nomCol, prenomCol, emailCol, telCol, roleCol, actifCol, actionsCol
        );

        tableView.getItems().setAll(userService.obtenirTousLesUtilisateurs());

        // Bouton refresh
        Button refreshBtn = new Button("🔄 Rafraîchir");
        refreshBtn.setOnAction(e ->
                tableView.getItems().setAll(userService.obtenirTousLesUtilisateurs())
        );

        root.getChildren().addAll(title, tableView, refreshBtn);
        return root;
    }
    private void showEditUserDialog(User user, TableView<User> tableView) {

        Stage dialog = new Stage();
        dialog.setTitle("Modifier utilisateur");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField nomField = new TextField(user.getNom());
        TextField prenomField = new TextField(user.getPrenom());
        TextField emailField = new TextField(user.getEmail());
        TextField telField = new TextField(user.getTelephone());

        ComboBox<Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(Role.ADMIN, Role.CLIENT);
        roleBox.setValue(user.getRole());

        CheckBox actifBox = new CheckBox("Actif");
        actifBox.setSelected(user.isActif());

        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        saveBtn.setOnAction(e -> {
            user.setNom(nomField.getText());
            user.setPrenom(prenomField.getText());
            user.setEmail(emailField.getText());
            user.setTelephone(telField.getText());
            user.setRole(roleBox.getValue());
            user.setActif(actifBox.isSelected());

            UserService service = new UserService();
            service.modifierUtilisateur(user);

            tableView.getItems().setAll(service.obtenirTousLesUtilisateurs());
            dialog.close();
        });

        root.getChildren().addAll(
                new Label("Nom"), nomField,
                new Label("Prénom"), prenomField,
                new Label("Email"), emailField,
                new Label("Téléphone"), telField,
                new Label("Rôle"), roleBox,
                actifBox,
                saveBtn
        );

        Scene scene = new Scene(root, 400, 500);
        dialog.setScene(scene);
        dialog.show();
    }


    private VBox createChambreManagementView() {

        ChambreService chambreService = new ChambreService();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Gestion des Chambres");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // ===== TableView =====
        TableView<Chambre> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Chambre, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Chambre, String> numCol = new TableColumn<>("Numéro");
        numCol.setCellValueFactory(new PropertyValueFactory<>("numero"));

        TableColumn<Chambre, TypeChambre> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Chambre, Double> prixCol = new TableColumn<>("Prix/Nuit");
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixParNuit"));

        TableColumn<Chambre, Integer> capCol = new TableColumn<>("Capacité");
        capCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));

        TableColumn<Chambre, StatutChambre> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        TableColumn<Chambre, Integer> etageCol = new TableColumn<>("Étage");
        etageCol.setCellValueFactory(new PropertyValueFactory<>("etage"));

        // ===== Actions =====
        TableColumn<Chambre, Void> actionsCol = new TableColumn<>("Actions");

        actionsCol.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(e -> {
                    Chambre chambre = getTableView().getItems().get(getIndex());
                    showEditChambreDialog(chambre, tableView);
                });

                deleteBtn.setOnAction(e -> {
                    Chambre chambre = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Supprimer cette chambre ?");

                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            chambreService.supprimerChambre(chambre.getId());
                            tableView.getItems().setAll(chambreService.obtenirToutesLesChambres());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableView.getColumns().addAll(
                idCol, numCol, typeCol, prixCol, capCol, statutCol, etageCol, actionsCol
        );

        tableView.getItems().setAll(chambreService.obtenirToutesLesChambres());

        // ===== Boutons =====
        Button addBtn = new Button("➕ Ajouter");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        Button refreshBtn = new Button("🔄 Rafraîchir");

        addBtn.setOnAction(e -> showAddChambreDialog(tableView));
        refreshBtn.setOnAction(e ->
                tableView.getItems().setAll(chambreService.obtenirToutesLesChambres())
        );

        HBox buttons = new HBox(10, addBtn, refreshBtn);

        root.getChildren().addAll(title, tableView, buttons);
        return root;
    }
    private void showAddChambreDialog(TableView<Chambre> tableView) {

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
            Chambre chambre = new Chambre();
            chambre.setNumero(numField.getText());
            chambre.setType(typeBox.getValue());
            chambre.setPrixParNuit(Double.parseDouble(prixField.getText()));
            chambre.setCapacite(Integer.parseInt(capField.getText()));
            chambre.setDescription(descArea.getText());
            chambre.setStatut(statutBox.getValue());
            chambre.setEtage(Integer.parseInt(etageField.getText()));

            ChambreService service = new ChambreService();
            service.ajouterChambre(chambre);

            tableView.getItems().setAll(service.obtenirToutesLesChambres());
            dialog.close();
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
    private void showEditChambreDialog(Chambre chambre, TableView<Chambre> tableView) {

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
            chambre.setNumero(numField.getText());
            chambre.setType(typeBox.getValue());
            chambre.setPrixParNuit(Double.parseDouble(prixField.getText()));
            chambre.setCapacite(Integer.parseInt(capField.getText()));
            chambre.setDescription(descArea.getText());
            chambre.setStatut(statutBox.getValue());
            chambre.setEtage(Integer.parseInt(etageField.getText()));

            ChambreService service = new ChambreService();
            service.modifierChambre(chambre);

            tableView.getItems().setAll(service.obtenirToutesLesChambres());
            dialog.close();
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

    private VBox createReservationManagementView() {
        VBox view = new VBox(15);
        Label title = new Label("Gestion des Réservations");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        view.getChildren().add(title);
        // TODO: Ajouter TableView avec liste des réservations
        return view;
    }

    // Afficher une alerte
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static User getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}