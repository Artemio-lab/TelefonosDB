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

    //Devuelve las personas ordenadas por ID
    public List<Persona> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre, direccion FROM Personas ORDER BY id";
        List<Persona> personas = new ArrayList<>();

        try (Connection conn = Conexion.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                personas.add(new Persona(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("direccion")
                ));
            }
        }
        return personas;
    }

    //Al momento de crear una persona devuelve su ID
    public int insertar(String nombre, String direccion) throws SQLException {
        String sql = "INSERT INTO Personas (nombre, direccion) VALUES (?, ?)";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setString(2, direccion);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    //Modifica una persona ya existente
    public boolean actualizar(int id, String nombre, String direccion) throws SQLException {
        String sql = "UPDATE Personas SET nombre = ?, direccion = ? WHERE id = ?";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, direccion);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    //Sirve para eliminar una persona, primero elimina todos sus numeros y luego a la persona
    public boolean eliminar(int id) throws SQLException {
        String sqlTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlPersona = "DELETE FROM Personas WHERE id = ?";

        try (Connection conn = Conexion.obtenerConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psTel = conn.prepareStatement(sqlTelefonos);
                 PreparedStatement psPer = conn.prepareStatement(sqlPersona)) {

                psTel.setInt(1, id);
                psTel.executeUpdate();

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
