package com.example.telefonosdb.GUI;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.telefonosdb.Logic.Persona;
import com.example.telefonosdb.Logic.PersonaRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class PersonasPanel extends BorderPane {

    private final PersonaRepository personaRepository;
    private final List<PersonasListener> listeners = new ArrayList<>();

    private final ObservableList<Persona> personasData = FXCollections.observableArrayList();
    private final TableView<Persona> tablaPersonas = new TableView<>();
    private final TextField txtNombre = new TextField();
    private Persona personaSeleccionada = null;

    public PersonasPanel(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
        construirUI();
        cargarPersonas();
    }

    public void agregarListener(PersonasListener listener) {
        listeners.add(listener);
    }

    private void construirUI() {
        setPadding(new Insets(10));

        TableColumn<Persona, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Persona, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(300);

        tablaPersonas.getColumns().addAll(List.of(colId, colNombre));
        tablaPersonas.setItems(personasData);
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, old, nueva) -> {
            personaSeleccionada = nueva;
            if (nueva != null) {
                txtNombre.setText(nueva.getNombre());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 10, 0));

        txtNombre.setPromptText("Nombre");
        form.add(new Label("Nombre:"), 0, 0);
        form.add(txtNombre, 1, 0);

        Button btnAgregar = new Button("Agregar");
        btnAgregar.setOnAction(e -> agregarPersona());

        Button btnModificar = new Button("Modificar");
        btnModificar.setOnAction(e -> modificarPersona());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarPersona());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        HBox botones = new HBox(10, btnAgregar, btnModificar, btnEliminar, btnLimpiar);
        botones.setAlignment(Pos.CENTER_LEFT);
        form.add(botones, 1, 1);

        setCenter(tablaPersonas);
        setBottom(form);
    }

    private void cargarPersonas() {
        try {
            personasData.setAll(personaRepository.listarTodas());
        } catch (SQLException e) {
            Dialogos.error("Error al cargar personas", e);
        }
    }

    private void agregarPersona() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            Dialogos.aviso("El nombre es obligatorio.");
            return;
        }
        try {
            personaRepository.insertar(nombre);
            cargarPersonas();
            notificarActualizacion();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al agregar la persona", e);
        }
    }

    private void modificarPersona() {
        if (personaSeleccionada == null) {
            Dialogos.aviso("Seleccioná una persona de la tabla para modificar.");
            return;
        }
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            Dialogos.aviso("El nombre es obligatorio.");
            return;
        }
        try {
            personaRepository.actualizar(personaSeleccionada.getId(), nombre);
            cargarPersonas();
            notificarActualizacion();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al modificar la persona", e);
        }
    }

    private void eliminarPersona() {
        if (personaSeleccionada == null) {
            Dialogos.aviso("Seleccioná una persona de la tabla para eliminar.");
            return;
        }

        boolean confirmado = Dialogos.confirmar("Confirmar eliminación",
                "¿Eliminar a \"" + personaSeleccionada.getNombre()
                        + "\"? Se quitarán sus teléfonos y sus asociaciones de dirección "
                        + "(las direcciones en sí no se borran si otras personas las usan).");
        if (!confirmado) {
            return;
        }
        try {
            int idEliminado = personaSeleccionada.getId();
            personaRepository.eliminar(idEliminado);
            cargarPersonas();
            notificarEliminacion(idEliminado);
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al eliminar la persona", e);
        }
    }

    private void limpiarFormulario() {
        personaSeleccionada = null;
        txtNombre.clear();
        tablaPersonas.getSelectionModel().clearSelection();
    }

    private void notificarActualizacion() {
        for (PersonasListener listener : listeners) {
            listener.onPersonasActualizadas();
        }
    }

    private void notificarEliminacion(int personaId) {
        for (PersonasListener listener : listeners) {
            listener.onPersonaEliminada(personaId);
        }
    }
}
