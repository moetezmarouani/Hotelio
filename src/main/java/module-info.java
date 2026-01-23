module com.example.hotelio {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Pour JavaFX (TableView, PropertyValueFactory)
    opens com.example.hotelio.entities to javafx.base;

    // Si tu utilises FXML plus tard
    opens com.example.hotelio to javafx.fxml;

    exports com.example.hotelio;
}
