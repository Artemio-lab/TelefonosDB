package com.example.telefonosdb.Logic;

import java.sql.SQLException;
import java.util.List;

public interface PersonaRepository {
    List<Persona> listarTodas() throws SQLException;
    int insertar(String nombre) throws SQLException;
    boolean actualizar(int id, String nombre) throws SQLException;
    boolean eliminar(int id) throws SQLException;
}
