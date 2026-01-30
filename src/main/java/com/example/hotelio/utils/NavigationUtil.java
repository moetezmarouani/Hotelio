package com.example.hotelio.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe utilitaire pour la navigation entre les vues FXML
 */
public class NavigationUtil {

    /**
     * Navigue vers une nouvelle vue FXML
     * @param source Le nœud source (généralement un bouton)
     * @param fxmlPath Le chemin du fichier FXML
     * @param title Le titre de la fenêtre
     */
    public static void navigateTo(Node source, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue: " + fxmlPath);
        }
    }

    /**
     * Charge une vue FXML et retourne le Parent
     * @param fxmlPath Le chemin du fichier FXML
     * @return Le Parent chargé
     */
    public static Parent loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        return loader.load();
    }
}