package com.example.telefonosdb.GUI;

import java.sql.SQLException;
import java.util.List;

import com.example.telefonosdb.Logic.Persona;
import com.example.telefonosdb.Logic.PersonaDAO;
import com.example.telefonosdb.Logic.Telefono;
import com.example.telefonosdb.Logic.TelefonoDAO;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Interfaz grafica la cual el usuario va a utilizar para interactuar
 * con el programa
 */
public class MainApp extends Application {

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final TelefonoDAO telefonoDAO = new TelefonoDAO();

    private final ObservableList<Persona> personasData = FXCollections.observableArrayList();
    private final TableView<Persona> tablaPersonas = new TableView<>();
    private final TextField txtNombre = new TextField();
    private final TextField txtDireccion = new TextField();
    private Persona personaSeleccionada = null;

    private final ObservableList<Telefono> telefonosData = FXCollections.observableArrayList();
    private final TableView<Telefono> tablaTelefonos = new TableView<>();
    private final ComboBox<Persona> comboPersonas = new ComboBox<>();
    private final TextField txtTelefono = new TextField();
    private Telefono telefonoSeleccionado = null;

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        Tab tabPersonas = new Tab("Personas", construirPanelPersonas());
        tabPersonas.setClosable(false);

        Tab tabTelefonos = new Tab("Teléfonos", construirPanelTelefonos());
        tabTelefonos.setClosable(false);

        tabPane.getTabs().addAll(tabPersonas, tabTelefonos);

        tabTelefonos.setOnSelectionChanged(e -> {
            if (tabTelefonos.isSelected()) {
                cargarComboPersonas();
            }
        });

        Scene scene = new Scene(tabPane, 800, 550);
        stage.setTitle("Agenda - Personas y Teléfonos");
        stage.setScene(scene);
        stage.show();

        cargarPersonas();
    }

    private BorderPane construirPanelPersonas() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        TableColumn<Persona, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Persona, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(220);

        TableColumn<Persona, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colDireccion.setPrefWidth(300);

        tablaPersonas.getColumns().addAll(List.of(colId, colNombre, colDireccion));
        tablaPersonas.setItems(personasData);
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, old, nueva) -> {
            personaSeleccionada = nueva;
            if (nueva != null) {
                txtNombre.setText(nueva.getNombre());
                txtDireccion.setText(nueva.getDireccion());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 10, 0));

        txtNombre.setPromptText("Nombre");
        txtDireccion.setPromptText("Dirección");

        form.add(new Label("Nombre:"), 0, 0);
        form.add(txtNombre, 1, 0);
        form.add(new Label("Dirección:"), 0, 1);
        form.add(txtDireccion, 1, 1);

        HBox botones = getHBox();
        form.add(botones, 1, 2);

        root.setCenter(tablaPersonas);
        root.setBottom(form);
        return root;
    }

    private HBox getHBox() {
        Button btnAgregar = new Button("Agregar");
        btnAgregar.setOnAction(e -> agregarPersona());

        Button btnModificar = new Button("Modificar");
        btnModificar.setOnAction(e -> modificarPersona());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarPersona());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormularioPersona());

        HBox botones = new HBox(10, btnAgregar, btnModificar, btnEliminar, btnLimpiar);
        botones.setAlignment(Pos.CENTER_LEFT);
        return botones;
    }

    private void cargarPersonas() {
        try {
            personasData.setAll(personaDAO.listarTodas());
        } catch (SQLException e) {
            mostrarError("Error al cargar personas", e);
        }
    }

    private void agregarPersona() {
        String nombre = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAviso("El nombre es obligatorio");
            return;
        }
        try {
            personaDAO.insertar(nombre, direccion);
            cargarPersonas();
            limpiarFormularioPersona();
        } catch (SQLException e) {
            mostrarError("Error al agregar la persona", e);
        }
    }

    private void modificarPersona() {
        if (personaSeleccionada == null) {
            mostrarAviso("Seleccioná una persona de la tabla para modificar");
            return;
        }
        String nombre = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAviso("El nombre es obligatorio");
            return;
        }
        try {
            personaDAO.actualizar(personaSeleccionada.getId(), nombre, direccion);
            cargarPersonas();
            limpiarFormularioPersona();
        } catch (SQLException e) {
            mostrarError("Error al modificar la persona", e);
        }
    }

    private void eliminarPersona() {
        if (personaSeleccionada == null) {
            mostrarAviso("Seleccioná una persona de la tabla para eliminar");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "¿Eliminar a \"" + personaSeleccionada.getNombre() + "\" y todos sus teléfonos?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    personaDAO.eliminar(personaSeleccionada.getId());
                    cargarPersonas();
                    limpiarFormularioPersona();
                } catch (SQLException e) {
                    mostrarError("Error al eliminar la persona", e);
                }
            }
        });
    }

    private void limpiarFormularioPersona() {
        personaSeleccionada = null;
        txtNombre.clear();
        txtDireccion.clear();
        tablaPersonas.getSelectionModel().clearSelection();
    }

    private BorderPane construirPanelTelefonos() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

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

        HBox botones = getBox();
        form.add(botones, 1, 1);

        BorderPane center = new BorderPane();
        center.setTop(topBox);
        center.setCenter(tablaTelefonos);

        root.setCenter(center);
        root.setBottom(form);
        return root;
    }

    private HBox getBox() {
        Button btnAgregar = new Button("Agregar");
        btnAgregar.setOnAction(e -> agregarTelefono());

        Button btnModificar = new Button("Modificar");
        btnModificar.setOnAction(e -> modificarTelefono());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarTelefono());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormularioTelefono());

        HBox botones = new HBox(10, btnAgregar, btnModificar, btnEliminar, btnLimpiar);
        botones.setAlignment(Pos.CENTER_LEFT);
        return botones;
    }

    private void cargarComboPersonas() {
        try {
            Persona actual = comboPersonas.getValue();
            comboPersonas.setItems(FXCollections.observableArrayList(personaDAO.listarTodas()));
            if (actual != null) {
                comboPersonas.setValue(actual);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar personas", e);
        }
    }

    private void cargarTelefonos() {
        Persona persona = comboPersonas.getValue();
        if (persona == null) {
            telefonosData.clear();
            return;
        }
        try {
            telefonosData.setAll(telefonoDAO.listarPorPersona(persona.getId()));
        } catch (SQLException e) {
            mostrarError("Error al cargar teléfonos", e);
        }
    }

    private void agregarTelefono() {
        Persona persona = comboPersonas.getValue();
        if (persona == null) {
            mostrarAviso("Seleccioná primero una persona.");
            return;
        }
        String telefono = txtTelefono.getText().trim();
        if (telefono.isEmpty()) {
            mostrarAviso("El teléfono es obligatorio.");
            return;
        }
        try {
            telefonoDAO.insertar(persona.getId(), telefono);
            cargarTelefonos();
            limpiarFormularioTelefono();
        } catch (SQLException e) {
            mostrarError("Error al agregar el teléfono", e);
        }
    }

    private void modificarTelefono() {
        if (telefonoSeleccionado == null) {
            mostrarAviso("Seleccioná un teléfono de la tabla para modificar.");
            return;
        }
        String telefono = txtTelefono.getText().trim();
        if (telefono.isEmpty()) {
            mostrarAviso("El teléfono es obligatorio.");
            return;
        }
        try {
            telefonoDAO.actualizar(telefonoSeleccionado.getId(), telefono);
            cargarTelefonos();
            limpiarFormularioTelefono();
        } catch (SQLException e) {
            mostrarError("Error al modificar el teléfono", e);
        }
    }

    private void eliminarTelefono() {
        if (telefonoSeleccionado == null) {
            mostrarAviso("Seleccioná un teléfono de la tabla para eliminar.");
            return;
        }
        try {
            telefonoDAO.eliminar(telefonoSeleccionado.getId());
            cargarTelefonos();
            limpiarFormularioTelefono();
        } catch (SQLException e) {
            mostrarError("Error al eliminar el teléfono", e);
        }
    }

    private void limpiarFormularioTelefono() {
        telefonoSeleccionado = null;
        txtTelefono.clear();
        tablaTelefonos.getSelectionModel().clearSelection();
    }

    private void mostrarAviso(String mensaje) {
        Alert alert = new Alert(AlertType.WARNING, mensaje, ButtonType.OK);
        alert.setTitle("Atención");
        alert.showAndWait();
    }

    private void mostrarError(String contexto, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(AlertType.ERROR, contexto + ":\n" + e.getMessage(), ButtonType.OK);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
