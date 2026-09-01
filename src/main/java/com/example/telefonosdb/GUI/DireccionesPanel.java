package com.example.telefonosdb.GUI;

import java.sql.SQLException;
import java.util.List;

import com.example.telefonosdb.Logic.Direccion;
import com.example.telefonosdb.Logic.DireccionRepository;
import com.example.telefonosdb.Logic.Persona;
import com.example.telefonosdb.Logic.PersonaRepository;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Pestaña "Direcciones": gestión de la relación muchos a muchos entre
 * Persona y Direccion.
 */
public class DireccionesPanel extends BorderPane implements PersonasListener {

    private final PersonaRepository personaRepository;
    private final DireccionRepository direccionRepository;

    private final ObservableList<Direccion> direccionesPersonaData = FXCollections.observableArrayList();
    private final TableView<Direccion> tablaDirecciones = new TableView<>();
    private final ComboBox<Persona> comboPersonas = new ComboBox<>();
    private final ComboBox<Direccion> comboCatalogo = new ComboBox<>();
    private final TextField txtDireccion = new TextField();
    private Direccion direccionSeleccionada = null;

    public DireccionesPanel(PersonaRepository personaRepository, DireccionRepository direccionRepository) {
        this.personaRepository = personaRepository;
        this.direccionRepository = direccionRepository;
        construirUI();
        cargarComboPersonas();
        cargarCatalogo();
    }

    @Override
    public void onPersonasActualizadas() {
        cargarComboPersonas();
    }

    @Override
    public void onPersonaEliminada(int personaId) {
        if (comboPersonas.getValue() != null && comboPersonas.getValue().getId() == personaId) {
            comboPersonas.setValue(null);
            direccionesPersonaData.clear();
        }
        cargarComboPersonas();
    }

    private void construirUI() {
        setPadding(new Insets(10));

        comboPersonas.setPromptText("Seleccioná una persona");
        comboPersonas.setPrefWidth(300);
        comboPersonas.valueProperty().addListener((obs, old, nueva) -> cargarDireccionesDePersona());

        HBox topBox = new HBox(10, new Label("Persona:"), comboPersonas);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(0, 0, 10, 0));

        TableColumn<Direccion, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Direccion, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colDireccion.setPrefWidth(320);

        tablaDirecciones.getColumns().addAll(List.of(colId, colDireccion));
        tablaDirecciones.setItems(direccionesPersonaData);
        tablaDirecciones.getSelectionModel().selectedItemProperty().addListener((obs, old, nueva) -> {
            direccionSeleccionada = nueva;
            if (nueva != null) {
                txtDireccion.setText(nueva.getDireccion());
            }
        });

        BorderPane center = new BorderPane();
        center.setTop(topBox);
        center.setCenter(tablaDirecciones);

        VBox form = new VBox(10);
        form.setPadding(new Insets(10, 0, 0, 0));

        HBox filaAsociar = new HBox(10);
        filaAsociar.setAlignment(Pos.CENTER_LEFT);
        comboCatalogo.setPromptText("Dirección existente del catálogo");
        comboCatalogo.setPrefWidth(280);
        Button btnAsociarExistente = new Button("Asociar existente a esta persona");
        btnAsociarExistente.setOnAction(e -> asociarExistente());
        filaAsociar.getChildren().addAll(new Label("Catálogo:"), comboCatalogo, btnAsociarExistente);

        HBox filaTexto = new HBox(10);
        filaTexto.setAlignment(Pos.CENTER_LEFT);
        txtDireccion.setPromptText("Texto de la dirección");
        txtDireccion.setPrefWidth(280);
        Button btnAgregarNueva = new Button("Crear nueva y asociar");
        btnAgregarNueva.setOnAction(e -> crearYAsociar());
        Button btnEditarTexto = new Button("Editar texto (afecta a todos)");
        btnEditarTexto.setOnAction(e -> editarTexto());
        filaTexto.getChildren().addAll(new Label("Texto:"), txtDireccion, btnAgregarNueva, btnEditarTexto);

        HBox filaAcciones = new HBox(10);
        filaAcciones.setAlignment(Pos.CENTER_LEFT);
        Button btnQuitar = new Button("Quitar de esta persona");
        btnQuitar.setOnAction(e -> desasociar());
        Button btnEliminarDef = new Button("Eliminar dirección (definitivo)");
        btnEliminarDef.setOnAction(e -> eliminarDefinitivo());
        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormulario());
        filaAcciones.getChildren().addAll(btnQuitar, btnEliminarDef, btnLimpiar);

        form.getChildren().addAll(filaAsociar, filaTexto, filaAcciones);

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

    private void cargarCatalogo() {
        try {
            Direccion actual = comboCatalogo.getValue();
            comboCatalogo.setItems(FXCollections.observableArrayList(direccionRepository.listarTodas()));
            if (actual != null) {
                comboCatalogo.getItems().stream()
                        .filter(d -> d.getId() == actual.getId())
                        .findFirst()
                        .ifPresent(comboCatalogo::setValue);
            }
        } catch (SQLException e) {
            Dialogos.error("Error al cargar el catálogo de direcciones", e);
        }
    }

    private void cargarDireccionesDePersona() {
        Persona persona = comboPersonas.getValue();
        if (persona == null) {
            direccionesPersonaData.clear();
            return;
        }
        try {
            direccionesPersonaData.setAll(direccionRepository.listarPorPersona(persona.getId()));
        } catch (SQLException e) {
            Dialogos.error("Error al cargar direcciones de la persona", e);
        }
    }

    private void asociarExistente() {
        Persona persona = comboPersonas.getValue();
        Direccion direccion = comboCatalogo.getValue();
        if (persona == null) {
            Dialogos.aviso("Seleccioná primero una persona.");
            return;
        }
        if (direccion == null) {
            Dialogos.aviso("Seleccioná una dirección del catálogo para asociar.");
            return;
        }
        try {
            direccionRepository.asociar(persona.getId(), direccion.getId());
            cargarDireccionesDePersona();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al asociar la dirección", e);
        }
    }

    private void crearYAsociar() {
        Persona persona = comboPersonas.getValue();
        if (persona == null) {
            Dialogos.aviso("Seleccioná primero una persona.");
            return;
        }
        String texto = txtDireccion.getText().trim();
        if (texto.isEmpty()) {
            Dialogos.aviso("El texto de la dirección es obligatorio.");
            return;
        }
        try {
            int idDireccion = direccionRepository.insertar(texto);
            direccionRepository.asociar(persona.getId(), idDireccion);
            cargarDireccionesDePersona();
            cargarCatalogo();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al crear la dirección", e);
        }
    }

    private void editarTexto() {
        if (direccionSeleccionada == null) {
            Dialogos.aviso("Seleccioná una dirección de la tabla para editar su texto.");
            return;
        }
        String texto = txtDireccion.getText().trim();
        if (texto.isEmpty()) {
            Dialogos.aviso("El texto de la dirección es obligatorio.");
            return;
        }

        boolean confirmado = Dialogos.confirmar("Confirmar edición",
                "Esta dirección puede estar compartida por varias personas. "
                        + "¿Confirmás modificar su texto para todas ellas?");
        if (!confirmado) {
            return;
        }
        try {
            direccionRepository.actualizar(direccionSeleccionada.getId(), texto);
            cargarDireccionesDePersona();
            cargarCatalogo();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al editar la dirección", e);
        }
    }

    private void desasociar() {
        Persona persona = comboPersonas.getValue();
        if (persona == null || direccionSeleccionada == null) {
            Dialogos.aviso("Seleccioná una persona y una dirección de la tabla.");
            return;
        }
        try {
            direccionRepository.desasociar(persona.getId(), direccionSeleccionada.getId());
            cargarDireccionesDePersona();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al quitar la asociación", e);
        }
    }

    private void eliminarDefinitivo() {
        if (direccionSeleccionada == null) {
            Dialogos.aviso("Seleccioná una dirección de la tabla para eliminar.");
            return;
        }

        boolean confirmado = Dialogos.confirmar("Confirmar eliminación definitiva",
                "Esto elimina la dirección \"" + direccionSeleccionada.getDireccion()
                        + "\" del catálogo por completo, incluso para las demás personas "
                        + "que la tengan asociada. ¿Confirmás?");
        if (!confirmado) {
            return;
        }
        try {
            direccionRepository.eliminar(direccionSeleccionada.getId());
            cargarDireccionesDePersona();
            cargarCatalogo();
            limpiarFormulario();
        } catch (SQLException e) {
            Dialogos.error("Error al eliminar la dirección", e);
        }
    }

    private void limpiarFormulario() {
        direccionSeleccionada = null;
        txtDireccion.clear();
        comboCatalogo.setValue(null);
        tablaDirecciones.getSelectionModel().clearSelection();
    }
}
