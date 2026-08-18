module com.example.telefonosdb {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.telefonosdb to javafx.fxml;
    exports com.example.telefonosdb;

    opens com.example.telefonosdb.GUI to javafx.graphics, javafx.fxml;
    exports com.example.telefonosdb.GUI;

    opens com.example.telefonosdb.Logic to javafx.base;
    exports com.example.telefonosdb.Logic;
}