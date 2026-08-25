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
 * Clase que contiene todos los metodos necesarios para un CRUD
 */
public class TelefonoDAO {

    /** Devuelve todos los teléfonos asociados a una persona. */
    public List<Telefono> listarPorPersona(int personaId) throws SQLException {
        String sql = "SELECT id, personaId, telefono FROM Telefonos WHERE personaId = ? ORDER BY id";
        List<Telefono> telefonos = new ArrayList<>();

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    telefonos.add(new Telefono(
                            rs.getInt("id"),
                            rs.getInt("personaId"),
                            rs.getString("telefono")
                    ));
                }
            }
        }
        return telefonos;
    }

    /** Alta de un nuevo teléfono para una persona. Devuelve el id generado. */
    public int insertar(int personaId, String telefono) throws SQLException {
        String sql = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, personaId);
            ps.setString(2, telefono);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /** Modificación del número de un teléfono existente. */
    public boolean actualizar(int id, String telefono) throws SQLException {
        String sql = "UPDATE Telefonos SET telefono = ? WHERE id = ?";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, telefono);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Baja de un teléfono por su id. */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Telefonos WHERE id = ?";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
