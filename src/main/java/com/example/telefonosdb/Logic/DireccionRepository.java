package com.example.telefonosdb.Logic;

import java.sql.SQLException;
import java.util.List;

public interface DireccionRepository {
    List<Direccion> listarTodas() throws SQLException;
    List<Direccion> listarPorPersona(int personaId) throws SQLException;
    List<Persona> listarPersonasPorDireccion(int direccionId) throws SQLException;
    int insertar(String direccion) throws SQLException;
    boolean actualizar(int id, String direccion) throws SQLException;
    boolean eliminar(int id) throws SQLException;
    boolean asociar(int personaId, int direccionId) throws SQLException;
    boolean desasociar(int personaId, int direccionId) throws SQLException;
}
