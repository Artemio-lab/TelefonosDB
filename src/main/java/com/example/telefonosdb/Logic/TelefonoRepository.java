package com.example.telefonosdb.Logic;

import java.sql.SQLException;
import java.util.List;

public interface TelefonoRepository {
    List<Telefono> listarPorPersona(int personaId) throws SQLException;
    int insertar(int personaId, String telefono) throws SQLException;
    boolean actualizar(int id, String telefono) throws SQLException;
    boolean eliminar(int id) throws SQLException;
}
