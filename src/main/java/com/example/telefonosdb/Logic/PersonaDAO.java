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

public class PersonaDAO implements PersonaRepository {

    private final ConexionProvider conexionProvider;

    public PersonaDAO() {
        this(new Conexion());
    }

    public PersonaDAO(ConexionProvider conexionProvider) {
        this.conexionProvider = conexionProvider;
    }

    @Override
    public List<Persona> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM Personas ORDER BY id";
        List<Persona> personas = new ArrayList<>();

        try (Connection conn = conexionProvider.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                personas.add(new Persona(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return personas;
    }

    @Override
    public int insertar(String nombre) throws SQLException {
        String sql = "INSERT INTO Personas (nombre) VALUES (?)";

        try (Connection conn = conexionProvider.obtenerConexion();
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

    @Override
    public boolean actualizar(int id, String nombre) throws SQLException {
        String sql = "UPDATE Personas SET nombre = ? WHERE id = ?";

        try (Connection conn = conexionProvider.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Baja de una persona. Se eliminan primero sus teléfonos
     */
    @Override
    public boolean eliminar(int id) throws SQLException {
        String sqlTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlDirecciones = "DELETE FROM PersonaDireccion WHERE personaId = ?";
        String sqlPersona = "DELETE FROM Personas WHERE id = ?";

        try (Connection conn = conexionProvider.obtenerConexion()) {
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
