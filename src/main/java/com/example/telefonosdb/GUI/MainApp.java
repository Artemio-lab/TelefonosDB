package com.example.telefonosdb.GUI;

import com.example.telefonosdb.Logic.DireccionDAO;
import com.example.telefonosdb.Logic.DireccionRepository;
import com.example.telefonosdb.Logic.PersonaDAO;
import com.example.telefonosdb.Logic.PersonaRepository;
import com.example.telefonosdb.Logic.TelefonoDAO;
import com.example.telefonosdb.Logic.TelefonoRepository;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // Único lugar donde se eligen las implementaciones concretas.
        PersonaRepository personaRepository = new PersonaDAO();
        TelefonoRepository telefonoRepository = new TelefonoDAO();
        DireccionRepository direccionRepository = new DireccionDAO();

        PersonasPanel personasPanel = new PersonasPanel(personaRepository);
        TelefonosPanel telefonosPanel = new TelefonosPanel(personaRepository, telefonoRepository);
        DireccionesPanel direccionesPanel = new DireccionesPanel(personaRepository, direccionRepository);

        // Se conectan los paneles a través de la interfaz PersonasListener,
        // sin que PersonasPanel conozca las clases concretas de los otros dos.
        personasPanel.agregarListener(telefonosPanel);
        personasPanel.agregarListener(direccionesPanel);

        Tab tabPersonas = new Tab("Personas", personasPanel);
        tabPersonas.setClosable(false);

        Tab tabTelefonos = new Tab("Teléfonos", telefonosPanel);
        tabTelefonos.setClosable(false);

        Tab tabDirecciones = new Tab("Direcciones", direccionesPanel);
        tabDirecciones.setClosable(false);

        TabPane tabPane = new TabPane(tabPersonas, tabTelefonos, tabDirecciones);

        Scene scene = new Scene(tabPane, 900, 580);
        stage.setTitle("Agenda - Personas, Teléfonos y Direcciones");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
