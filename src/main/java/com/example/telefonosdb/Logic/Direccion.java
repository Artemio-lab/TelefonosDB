package com.example.telefonosdb.Logic;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo que representa una fila del catálogo de la tabla Direcciones.
 */
public class Direccion {

    private final IntegerProperty id;
    private final StringProperty direccion;

    public Direccion(int id, String direccion) {
        this.id = new SimpleIntegerProperty(id);
        this.direccion = new SimpleStringProperty(direccion);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getDireccion() {
        return direccion.get();
    }

    public void setDireccion(String direccion) {
        this.direccion.set(direccion);
    }

    public StringProperty direccionProperty() {
        return direccion;
    }

    @Override
    public String toString() {
        return direccion.get() + " (ID: " + id.get() + ")";
    }
}
