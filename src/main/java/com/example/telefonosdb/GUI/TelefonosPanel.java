package com.example.telefonosdb.GUI;

import java.sql.SQLException;
import java.util.List;

import com.example.telefonosdb.Logic.Persona;
import com.example.telefonosdb.Logic.PersonaRepository;
import com.example.telefonosdb.Logic.Telefono;
import com.example.telefonosdb.Logic.TelefonoRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class TelefonosPanel extends BorderPane implements PersonasListener {

    private final PersonaRepository personaRepository;
    private final TelefonoRepository telefonoRepository;

    private final ObservableList<Telefono> telefonosData = FXCollections.observableArrayList();
    private final TableView<Telefono> tablaTelefonos = new TableView<>();
    private final ComboBox<Persona> comboPersonas = new ComboBox<>();
    private final TextField txtTelefono = new TextField();
    private Telefono telefonoSeleccionado = null;

    public TelefonosPanel(PersonaRepository personaRepository, TelefonoRepository telefonoRepository) {
        this.personaRepository = personaRepository;
        this.telefonoRepository = telefonoRepository;
        construirUI();
        cargarComboPersonas();
    }

    @Override
    public void onPersonasActualizadas() {
        cargarComboPersonas();
    }

    @Override
    public void onPersonaEliminada(int personaId) {
        if (comboPersonas.getValue() != null && comboPersonas.getValue().getId() == personaId) {
            comboPersonas.setValue(null);
            telefonosData.clear();
        }
        cargarComboPersonas();
    }

    private void construirUI() {
        setPadding(new Insets(10));

        comboPersonas.setPromptText("Seleccioná una persona");
        comboPersonas.setPrefWidth(300);
        comboPersonas.valueProperty().addListener((obs, old, nueva) -> cargarTelefonos());

        HBox topBox = new HBox(10, new Label("Persona:"), comboPersonas);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(0, 0, 10, 0));

        TableColumn<Telefono, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Telefono, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(200);

        tablaTelefonos.getColumns().addAll(List.of(colId, colTelefono));
        tablaTelefonos.setItems(telefonosData);
        tablaTelefonos.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            telefonoSeleccionado = nuevo;
            if (nuevo != null) {
                txtTelefono.setText(nuevo.getTelefono());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 10, 0));

        txtTelefono.setPromptText("Número de teléfono");
        form.add(new Label("Teléfono:"), 0, 0);
        form.add(txtTelefono, 1, 0);

        Button btnAgregar = new Button("Agregar");
        btnAgregar.setOnAction(e -> agregarTelefono());

        Button btnModificar = new Button("Modificar");
        btnModificar.setOnAction(e -> modificarTelefono());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarTelefono());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        HBox botones = new HBox(10, btnAgregar, btnModificar, btnEliminar, btnLimpiar);
        botones.setAlignment(Pos.CENTER_LEFT);
        form.add(botones, 1, 1);

        BorderPane center = new BorderPane();
        center.setTop(topBox);
        center.setCenter(tablaTelefonos);

        setCenter(center);
        setBottom(form);
    }

    private void cargarComboPersonas() {
        try {
            Persona actual = comboPersonas.getValue();
            comboPersonas.setItems(FXCollections.observableArrayList(personaRepository.listarTodas()));
            if (actual != null) {
                comboPersonas.getItems().stream()
                        .filter(p -> p.getId() == actual.getId())
                        .findFirst()
                        .ifPresent(comboPersonas::setValue);
            }
        } catch (SQLException e) {
            Dialogos.error("Error al cargar personas", e);
        }
    }

    private void cargarTelefonos() {
        Persona persona = comboPersonas.getValue();
        if (persona == null) {
            telefonosData.clear();
            return;
        }
        try {
            telefonosData.setAll(telefonoRepository.listarPorPersona(persona.getId()));
        } catch (SQLException e) {
            Dialogos.error("Error al cargar teléfonos", e);
        }
    }

    private void agregarTelefono() {
        Persona persona = comboPersonas.getValue();
        if (persona == null) {
            Dialogos.aviso("Seleccioná primero una persona.");
            return;
        }
        String telefono = txtTelefono.getText().trim();
        if (telefono.isEmpty()) {
            Dialogos.aviso("El teléfono es obligatorio.");
            return;
        }
        try {
            telefonoRepository.insertar(persona.getId(), telefono);
            cargarTelefonos();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al agregar el teléfono", e);
        }
    }

    private void modificarTelefono() {
        if (telefonoSeleccionado == null) {
            Dialogos.aviso("Seleccioná un teléfono de la tabla para modificar.");
            return;
        }
        String telefono = txtTelefono.getText().trim();
        if (telefono.isEmpty()) {
            Dialogos.aviso("El teléfono es obligatorio.");
            return;
        }
        try {
            telefonoRepository.actualizar(telefonoSeleccionado.getId(), telefono);
            cargarTelefonos();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al modificar el teléfono", e);
        }
    }

    private void eliminarTelefono() {
        if (telefonoSeleccionado == null) {
            Dialogos.aviso("Seleccioná un teléfono de la tabla para eliminar.");
            return;
        }
        try {
            telefonoRepository.eliminar(telefonoSeleccionado.getId());
            cargarTelefonos();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al eliminar el teléfono", e);
        }
    }

    private void limpiarFormulario() {
        telefonoSeleccionado = null;
        txtTelefono.clear();
        tablaTelefonos.getSelectionModel().clearSelection();
    }
}
