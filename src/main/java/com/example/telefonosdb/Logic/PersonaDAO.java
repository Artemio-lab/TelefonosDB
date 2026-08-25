package com.example.telefonosdb.Logic;

import com.example.telefonosdb.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase la cual contiene todas las funciones de las personas que necesita un CRUD
 */
public class PersonaDAO {

    /** Devuelve todas las personas ordenadas por id. */
    public List<Persona> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM Personas ORDER BY id";
        List<Persona> personas = new ArrayList<>();

        try (Connection conn = Conexion.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                personas.add(new Persona(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return personas;
    }

    /** Alta de una nueva persona. Devuelve el id generado. */
    public int insertar(String nombre) throws SQLException {
        String sql = "INSERT INTO Personas (nombre) VALUES (?)";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /** Modificación del nombre de una persona existente. */
    public boolean actualizar(int id, String nombre) throws SQLException {
        String sql = "UPDATE Personas SET nombre = ? WHERE id = ?";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sqlTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlDirecciones = "DELETE FROM PersonaDireccion WHERE personaId = ?";
        String sqlPersona = "DELETE FROM Personas WHERE id = ?";

        try (Connection conn = Conexion.obtenerConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psTel = conn.prepareStatement(sqlTelefonos);
                 PreparedStatement psDir = conn.prepareStatement(sqlDirecciones);
                 PreparedStatement psPer = conn.prepareStatement(sqlPersona)) {

                psTel.setInt(1, id);
                psTel.executeUpdate();

                psDir.setInt(1, id);
                psDir.executeUpdate();

                psPer.setInt(1, id);
                int filas = psPer.executeUpdate();

                conn.commit();
                return filas > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
