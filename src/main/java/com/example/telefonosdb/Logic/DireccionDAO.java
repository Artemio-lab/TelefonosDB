package com.example.telefonosdb.Logic;

import com.example.telefonosdb.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DireccionDAO {

    /** Devuelve el catálogo completo de direcciones (todas, sin importar a quién pertenecen). */
    public List<Direccion> listarTodas() throws SQLException {
        String sql = "SELECT id, direccion FROM Direcciones ORDER BY direccion";
        List<Direccion> direcciones = new ArrayList<>();

        try (Connection conn = Conexion.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                direcciones.add(new Direccion(rs.getInt("id"), rs.getString("direccion")));
            }
        }
        return direcciones;
    }

    /** Devuelve las direcciones asociadas a una persona específica. */
    public List<Direccion> listarPorPersona(int personaId) throws SQLException {
        String sql = """
                SELECT d.id, d.direccion
                FROM Direcciones d
                JOIN PersonaDireccion pd ON pd.direccionId = d.id
                WHERE pd.personaId = ?
                ORDER BY d.direccion
                """;
        List<Direccion> direcciones = new ArrayList<>();

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    direcciones.add(new Direccion(rs.getInt("id"), rs.getString("direccion")));
                }
            }
        }
        return direcciones;
    }

    /** Devuelve las personas que comparten una dirección dada. Útil para mostrar quién más vive ahí. */
    public List<Persona> listarPersonasPorDireccion(int direccionId) throws SQLException {
        String sql = """
                SELECT p.id, p.nombre
                FROM Personas p
                JOIN PersonaDireccion pd ON pd.personaId = p.id
                WHERE pd.direccionId = ?
                ORDER BY p.nombre
                """;
        List<Persona> personas = new ArrayList<>();

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, direccionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    personas.add(new Persona(rs.getInt("id"), rs.getString("nombre")));
                }
            }
        }
        return personas;
    }

    /** Alta de una dirección nueva en el catálogo (todavía sin asociar a nadie). Devuelve el id generado. */
    public int insertar(String direccion) throws SQLException {
        String sql = "INSERT INTO Direcciones (direccion) VALUES (?)";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, direccion);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Modifica el texto de una dirección existente. Como la dirección puede
     * estar compartida, este cambio afecta a TODAS las personas asociadas.
     */
    public boolean actualizar(int id, String direccion) throws SQLException {
        String sql = "UPDATE Direcciones SET direccion = ? WHERE id = ?";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, direccion);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una dirección del catálogo por completo, junto con todas sus
     * asociaciones a personas (afecta a todos los que la compartían).
     */
    public boolean eliminar(int id) throws SQLException {
        String sqlAsociaciones = "DELETE FROM PersonaDireccion WHERE direccionId = ?";
        String sqlDireccion = "DELETE FROM Direcciones WHERE id = ?";

        try (Connection conn = Conexion.obtenerConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psAsoc = conn.prepareStatement(sqlAsociaciones);
                 PreparedStatement psDir = conn.prepareStatement(sqlDireccion)) {

                psAsoc.setInt(1, id);
                psAsoc.executeUpdate();

                psDir.setInt(1, id);
                int filas = psDir.executeUpdate();

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

    /**
     * Asocia una dirección existente del catálogo a una persona (relación N:M).
     * Si ya estaban asociadas, no hace nada (evita duplicados en la clave compuesta).
     */
    public boolean asociar(int personaId, int direccionId) throws SQLException {
        String sql = "INSERT IGNORE INTO PersonaDireccion (personaId, direccionId) VALUES (?, ?)";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personaId);
            ps.setInt(2, direccionId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Quita la asociación entre una persona y una dirección, sin borrar la
     * dirección del catálogo (puede seguir usándola otra persona).
     */
    public boolean desasociar(int personaId, int direccionId) throws SQLException {
        String sql = "DELETE FROM PersonaDireccion WHERE personaId = ? AND direccionId = ?";

        try (Connection conn = Conexion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personaId);
            ps.setInt(2, direccionId);
            return ps.executeUpdate() > 0;
        }
    }
}