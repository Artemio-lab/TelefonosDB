package com.example.telefonosdb.GUI;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Punto único para mostrar avisos, errores y confirmaciones.
 */
public final class Dialogos {

    private Dialogos() {
        // Clase utilitaria: no se instancia.
    }

    public static void aviso(String mensaje) {
        Alert alert = new Alert(AlertType.WARNING, mensaje, ButtonType.OK);
        alert.setTitle("Atención");
        alert.showAndWait();
    }

    public static void error(String contexto, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(AlertType.ERROR, contexto + ":\n" + e.getMessage(), ButtonType.OK);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    /** Muestra una confirmación Sí/No y devuelve true si el usuario eligió "Sí". */
    public static boolean confirmar(String titulo, String mensaje) {
        Alert confirm = new Alert(AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        confirm.setTitle(titulo);
        return confirm.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }
}
