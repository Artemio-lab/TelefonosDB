package com.example.telefonosdb.Logic;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Clase base para los telefonos de las personas
 */
public class Telefono {

    private final IntegerProperty id;
    private final IntegerProperty personaId;
    private final StringProperty telefono;

    public Telefono(int id, int personaId, String telefono) {
        this.id = new SimpleIntegerProperty(id);
        this.personaId = new SimpleIntegerProperty(personaId);
        this.telefono = new SimpleStringProperty(telefono);
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

    public int getPersonaId() {
        return personaId.get();
    }

    public void setPersonaId(int personaId) {
        this.personaId.set(personaId);
    }

    public IntegerProperty personaIdProperty() {
        return personaId;
    }

    public String getTelefono() {
        return telefono.get();
    }

    public void setTelefono(String telefono) {
        this.telefono.set(telefono);
    }

    public StringProperty telefonoProperty() {
        return telefono;
    }
}
