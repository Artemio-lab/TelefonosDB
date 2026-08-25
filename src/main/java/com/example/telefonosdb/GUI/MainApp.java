package com.example.telefonosdb.GUI;

import java.sql.SQLException;
import java.util.List;

import com.example.telefonosdb.Logic.*;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Interfaz grafica la cual el usuario va a utilizar para interactuar
 * con el programa
 */

public class MainApp extends Application {

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final TelefonoDAO telefonoDAO = new TelefonoDAO();
    private final DireccionDAO direccionDAO = new DireccionDAO();

    // --- Estado de la pestaña Personas ---
    private final ObservableList<Persona> personasData = FXCollections.observableArrayList();
    private final TableView<Persona> tablaPersonas = new TableView<>();
    private final TextField txtNombre = new TextField();
    private Persona personaSeleccionada = null;

    // --- Estado de la pestaña Telefonos ---
    private final ObservableList<Telefono> telefonosData = FXCollections.observableArrayList();
    private final TableView<Telefono> tablaTelefonos = new TableView<>();
    private final ComboBox<Persona> comboPersonasTel = new ComboBox<>();
    private final TextField txtTelefono = new TextField();
    private Telefono telefonoSeleccionado = null;

    // --- Estado de la pestaña Direcciones ---
    private final ObservableList<Direccion> direccionesPersonaData = FXCollections.observableArrayList();
    private final TableView<Direccion> tablaDireccionesPersona = new TableView<>();
    private final ComboBox<Persona> comboPersonasDir = new ComboBox<>();
    private final ComboBox<Direccion> comboCatalogoDirecciones = new ComboBox<>();
    private final TextField txtDireccionNueva = new TextField();
    private Direccion direccionSeleccionada = null;

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        Tab tabPersonas = new Tab("Personas", construirPanelPersonas());
        tabPersonas.setClosable(false);

        Tab tabTelefonos = new Tab("Teléfonos", construirPanelTelefonos());
        tabTelefonos.setClosable(false);

        Tab tabDirecciones = new Tab("Direcciones", construirPanelDirecciones());
        tabDirecciones.setClosable(false);

        tabPane.getTabs().addAll(tabPersonas, tabTelefonos, tabDirecciones);

        Scene scene = new Scene(tabPane, 900, 580);
        stage.setTitle("Agenda - Personas, Teléfonos y Direcciones");
        stage.setScene(scene);
        stage.show();

        // Se precargan todos los combos al iniciar, para no depender de
        // que el usuario visite cada pestaña antes de poder usarla.
        cargarPersonas();
        cargarComboPersonasTel();
        cargarComboPersonasDir();
        cargarCatalogoDirecciones();
    }


    private BorderPane construirPanelPersonas() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

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
        btnLimpiar.setOnAction(e -> limpiarFormularioPersona());

        HBox botones = new HBox(10, btnAgregar, btnModificar, btnEliminar, btnLimpiar);
        botones.setAlignment(Pos.CENTER_LEFT);
        form.add(botones, 1, 1);

        root.setCenter(tablaPersonas);
        root.setBottom(form);
        return root;
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
        if (nombre.isEmpty()) {
            mostrarAviso("El nombre es obligatorio.");
            return;
        }
        try {
            personaDAO.insertar(nombre);
            cargarPersonas();
            refrescarCombosDePersonas();
            limpiarFormularioPersona();
        } catch (SQLException e) {
            mostrarError("Error al agregar la persona", e);
        }
    }

    private void modificarPersona() {
        if (personaSeleccionada == null) {
            mostrarAviso("Seleccioná una persona de la tabla para modificar.");
            return;
        }
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarAviso("El nombre es obligatorio.");
            return;
        }
        try {
            personaDAO.actualizar(personaSeleccionada.getId(), nombre);
            cargarPersonas();
            refrescarCombosDePersonas();
            limpiarFormularioPersona();
        } catch (SQLException e) {
            mostrarError("Error al modificar la persona", e);
        }
    }

    private void eliminarPersona() {
        if (personaSeleccionada == null) {
            mostrarAviso("Seleccioná una persona de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "¿Eliminar a \"" + personaSeleccionada.getNombre()
                        + "\"? Se quitarán sus teléfonos y sus asociaciones de dirección "
                        + "(las direcciones en sí no se borran si otras personas las usan).",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    int idEliminado = personaSeleccionada.getId();
                    personaDAO.eliminar(idEliminado);
                    cargarPersonas();
                    if (comboPersonasTel.getValue() != null
                            && comboPersonasTel.getValue().getId() == idEliminado) {
                        comboPersonasTel.setValue(null);
                        telefonosData.clear();
                    }
                    if (comboPersonasDir.getValue() != null
                            && comboPersonasDir.getValue().getId() == idEliminado) {
                        comboPersonasDir.setValue(null);
                        direccionesPersonaData.clear();
                    }
                    refrescarCombosDePersonas();
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
        tablaPersonas.getSelectionModel().clearSelection();
    }

    /** Refresca los combos de persona de las pestañas Teléfonos y Direcciones a la vez. */
    private void refrescarCombosDePersonas() {
        cargarComboPersonasTel();
        cargarComboPersonasDir();
    }

    private BorderPane construirPanelTelefonos() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        comboPersonasTel.setPromptText("Seleccioná una persona");
        comboPersonasTel.setPrefWidth(300);
        comboPersonasTel.valueProperty().addListener((obs, old, nueva) -> cargarTelefonos());

        HBox topBox = new HBox(10, new Label("Persona:"), comboPersonasTel);
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
        btnLimpiar.setOnAction(e -> limpiarFormularioTelefono());

        HBox botones = new HBox(10, btnAgregar, btnModificar, btnEliminar, btnLimpiar);
        botones.setAlignment(Pos.CENTER_LEFT);
        form.add(botones, 1, 1);

        BorderPane center = new BorderPane();
        center.setTop(topBox);
        center.setCenter(tablaTelefonos);

        root.setCenter(center);
        root.setBottom(form);
        return root;
    }

    private void cargarComboPersonasTel() {
        recargarComboPersonas(comboPersonasTel);
    }

    private void cargarTelefonos() {
        Persona persona = comboPersonasTel.getValue();
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
        Persona persona = comboPersonasTel.getValue();
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


    private BorderPane construirPanelDirecciones() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        comboPersonasDir.setPromptText("Seleccioná una persona");
        comboPersonasDir.setPrefWidth(300);
        comboPersonasDir.valueProperty().addListener((obs, old, nueva) -> cargarDireccionesDePersona());

        HBox topBox = new HBox(10, new Label("Persona:"), comboPersonasDir);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(0, 0, 10, 0));

        TableColumn<Direccion, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Direccion, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colDireccion.setPrefWidth(320);

        tablaDireccionesPersona.getColumns().addAll(List.of(colId, colDireccion));
        tablaDireccionesPersona.setItems(direccionesPersonaData);
        tablaDireccionesPersona.getSelectionModel().selectedItemProperty().addListener((obs, old, nueva) -> {
            direccionSeleccionada = nueva;
            if (nueva != null) {
                txtDireccionNueva.setText(nueva.getDireccion());
            }
        });

        BorderPane center = new BorderPane();
        center.setTop(topBox);
        center.setCenter(tablaDireccionesPersona);

        VBox form = new VBox(10);
        form.setPadding(new Insets(10, 0, 0, 0));

        HBox filaAsociar = new HBox(10);
        filaAsociar.setAlignment(Pos.CENTER_LEFT);
        comboCatalogoDirecciones.setPromptText("Dirección existente del catálogo");
        comboCatalogoDirecciones.setPrefWidth(280);
        Button btnAsociarExistente = new Button("Asociar existente a esta persona");
        btnAsociarExistente.setOnAction(e -> asociarDireccionExistente());
        filaAsociar.getChildren().addAll(new Label("Catálogo:"), comboCatalogoDirecciones, btnAsociarExistente);

        HBox filaTexto = new HBox(10);
        filaTexto.setAlignment(Pos.CENTER_LEFT);
        txtDireccionNueva.setPromptText("Texto de la dirección");
        txtDireccionNueva.setPrefWidth(280);
        Button btnAgregarNueva = new Button("Crear nueva y asociar");
        btnAgregarNueva.setOnAction(e -> crearYAsociarDireccion());
        Button btnEditarTexto = new Button("Editar texto (afecta a todos)");
        btnEditarTexto.setOnAction(e -> editarTextoDireccion());
        filaTexto.getChildren().addAll(new Label("Texto:"), txtDireccionNueva, btnAgregarNueva, btnEditarTexto);

        HBox filaAcciones = new HBox(10);
        filaAcciones.setAlignment(Pos.CENTER_LEFT);
        Button btnQuitar = new Button("Quitar de esta persona");
        btnQuitar.setOnAction(e -> desasociarDireccion());
        Button btnEliminarDef = new Button("Eliminar dirección (definitivo)");
        btnEliminarDef.setOnAction(e -> eliminarDireccionDefinitivo());
        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormularioDireccion());
        filaAcciones.getChildren().addAll(btnQuitar, btnEliminarDef, btnLimpiar);

        form.getChildren().addAll(filaAsociar, filaTexto, filaAcciones);

        root.setCenter(center);
        root.setBottom(form);
        return root;
    }

    private void cargarComboPersonasDir() {
        recargarComboPersonas(comboPersonasDir);
    }

    private void cargarCatalogoDirecciones() {
        try {
            Direccion actual = comboCatalogoDirecciones.getValue();
            comboCatalogoDirecciones.setItems(FXCollections.observableArrayList(direccionDAO.listarTodas()));
            if (actual != null) {
                comboCatalogoDirecciones.getItems().stream()
                        .filter(d -> d.getId() == actual.getId())
                        .findFirst()
                        .ifPresent(comboCatalogoDirecciones::setValue);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar el catálogo de direcciones", e);
        }
    }

    private void cargarDireccionesDePersona() {
        Persona persona = comboPersonasDir.getValue();
        if (persona == null) {
            direccionesPersonaData.clear();
            return;
        }
        try {
            direccionesPersonaData.setAll(direccionDAO.listarPorPersona(persona.getId()));
        } catch (SQLException e) {
            mostrarError("Error al cargar direcciones de la persona", e);
        }
    }

    private void asociarDireccionExistente() {
        Persona persona = comboPersonasDir.getValue();
        Direccion direccion = comboCatalogoDirecciones.getValue();
        if (persona == null) {
            mostrarAviso("Seleccioná primero una persona.");
            return;
        }
        if (direccion == null) {
            mostrarAviso("Seleccioná una dirección del catálogo para asociar.");
            return;
        }
        try {
            direccionDAO.asociar(persona.getId(), direccion.getId());
            cargarDireccionesDePersona();
            limpiarFormularioDireccion();
        } catch (SQLException e) {
            mostrarError("Error al asociar la dirección", e);
        }
    }

    private void crearYAsociarDireccion() {
        Persona persona = comboPersonasDir.getValue();
        if (persona == null) {
            mostrarAviso("Seleccioná primero una persona.");
            return;
        }
        String texto = txtDireccionNueva.getText().trim();
        if (texto.isEmpty()) {
            mostrarAviso("El texto de la dirección es obligatorio.");
            return;
        }
        try {
            int idDireccion = direccionDAO.insertar(texto);
            direccionDAO.asociar(persona.getId(), idDireccion);
            cargarDireccionesDePersona();
            cargarCatalogoDirecciones();
            limpiarFormularioDireccion();
        } catch (SQLException e) {
            mostrarError("Error al crear la dirección", e);
        }
    }

    private void editarTextoDireccion() {
        if (direccionSeleccionada == null) {
            mostrarAviso("Seleccioná una dirección de la tabla para editar su texto.");
            return;
        }
        String texto = txtDireccionNueva.getText().trim();
        if (texto.isEmpty()) {
            mostrarAviso("El texto de la dirección es obligatorio.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Esta dirección puede estar compartida por varias personas. "
                        + "¿Confirmás modificar su texto para todas ellas?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar edición");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    direccionDAO.actualizar(direccionSeleccionada.getId(), texto);
                    cargarDireccionesDePersona();
                    cargarCatalogoDirecciones();
                    limpiarFormularioDireccion();
                } catch (SQLException e) {
                    mostrarError("Error al editar la dirección", e);
                }
            }
        });
    }

    private void desasociarDireccion() {
        Persona persona = comboPersonasDir.getValue();
        if (persona == null || direccionSeleccionada == null) {
            mostrarAviso("Seleccioná una persona y una dirección de la tabla.");
            return;
        }
        try {
            direccionDAO.desasociar(persona.getId(), direccionSeleccionada.getId());
            cargarDireccionesDePersona();
            limpiarFormularioDireccion();
        } catch (SQLException e) {
            mostrarError("Error al quitar la asociación", e);
        }
    }

    private void eliminarDireccionDefinitivo() {
        if (direccionSeleccionada == null) {
            mostrarAviso("Seleccioná una dirección de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Esto elimina la dirección \"" + direccionSeleccionada.getDireccion()
                        + "\" del catálogo por completo, incluso para las demás personas "
                        + "que la tengan asociada. ¿Confirmás?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación definitiva");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    direccionDAO.eliminar(direccionSeleccionada.getId());
                    cargarDireccionesDePersona();
                    cargarCatalogoDirecciones();
                    limpiarFormularioDireccion();
                } catch (SQLException e) {
                    mostrarError("Error al eliminar la dirección", e);
                }
            }
        });
    }

    private void limpiarFormularioDireccion() {
        direccionSeleccionada = null;
        txtDireccionNueva.clear();
        comboCatalogoDirecciones.setValue(null);
        tablaDireccionesPersona.getSelectionModel().clearSelection();
    }

    /** Recarga un ComboBox de personas cualquiera, preservando la selección si sigue existiendo. */
    private void recargarComboPersonas(ComboBox<Persona> combo) {
        try {
            Persona actual = combo.getValue();
            combo.setItems(FXCollections.observableArrayList(personaDAO.listarTodas()));
            if (actual != null) {
                combo.getItems().stream()
                        .filter(p -> p.getId() == actual.getId())
                        .findFirst()
                        .ifPresent(combo::setValue);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar personas", e);
        }
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
