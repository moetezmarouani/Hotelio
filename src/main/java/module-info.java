module com.example.hotelio {
    // Modules JavaFX requis
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Module MySQL
    requires java.sql;

    // Exporter les packages pour que JavaFX puisse y accéder
    opens com.example.hotelio to javafx.fxml;
    opens com.example.hotelio.controllers to javafx.fxml;
    opens com.example.hotelio.entities to javafx.base;

    // Exporter les packages publics
    exports com.example.hotelio;
    exports com.example.hotelio.controllers;
    exports com.example.hotelio.entities;
    exports com.example.hotelio.enums;
}