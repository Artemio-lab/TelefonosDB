package com.example.telefonosdb.Logic;

import com.example.telefonosdb.Conexion;
import com.example.telefonosdb.ConexionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DireccionDAO implements DireccionRepository {

    private final ConexionProvider conexionProvider;

    public DireccionDAO() {
        this(new Conexion());
    }

    public DireccionDAO(ConexionProvider conexionProvider) {
        this.conexionProvider = conexionProvider;
    }

    @Override
    public List<Direccion> listarTodas() throws SQLException {
        String sql = "SELECT id, direccion FROM Direcciones ORDER BY direccion";
        List<Direccion> direcciones = new ArrayList<>();

        try (Connection conn = conexionProvider.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                direcciones.add(new Direccion(rs.getInt("id"), rs.getString("direccion")));
            }
        }
        return direcciones;
    }

    @Override
    public List<Direccion> listarPorPersona(int personaId) throws SQLException {
        String sql = """
                SELECT d.id, d.direccion
                FROM Direcciones d
                JOIN PersonaDireccion pd ON pd.direccionId = d.id
                WHERE pd.personaId = ?
                ORDER BY d.direccion
                """;
        List<Direccion> direcciones = new ArrayList<>();

        try (Connection conn = conexionProvider.obtenerConexion();
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

    @Override
    public List<Persona> listarPersonasPorDireccion(int direccionId) throws SQLException {
        String sql = """
                SELECT p.id, p.nombre
                FROM Personas p
                JOIN PersonaDireccion pd ON pd.personaId = p.id
                WHERE pd.direccionId = ?
                ORDER BY p.nombre
                """;
        List<Persona> personas = new ArrayList<>();

        try (Connection conn = conexionProvider.obtenerConexion();
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

    @Override
    public int insertar(String direccion) throws SQLException {
        String sql = "INSERT INTO Direcciones (direccion) VALUES (?)";

        try (Connection conn = conexionProvider.obtenerConexion();
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

    @Override
    public boolean actualizar(int id, String direccion) throws SQLException {
        String sql = "UPDATE Direcciones SET direccion = ? WHERE id = ?";

        try (Connection conn = conexionProvider.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, direccion);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sqlAsociaciones = "DELETE FROM PersonaDireccion WHERE direccionId = ?";
        String sqlDireccion = "DELETE FROM Direcciones WHERE id = ?";

        try (Connection conn = conexionProvider.obtenerConexion()) {
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

    @Override
    public boolean asociar(int personaId, int direccionId) throws SQLException {
        String sql = "INSERT IGNORE INTO PersonaDireccion (personaId, direccionId) VALUES (?, ?)";

        try (Connection conn = conexionProvider.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personaId);
            ps.setInt(2, direccionId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean desasociar(int personaId, int direccionId) throws SQLException {
        String sql = "DELETE FROM PersonaDireccion WHERE personaId = ? AND direccionId = ?";

        try (Connection conn = conexionProvider.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personaId);
            ps.setInt(2, direccionId);
            return ps.executeUpdate() > 0;
        }
    }
}
