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
public class TelefonoDAO implements TelefonoRepository {

    private final ConexionProvider conexionProvider;

    public TelefonoDAO() {
        this(new Conexion());
    }

    public TelefonoDAO(ConexionProvider conexionProvider) {
        this.conexionProvider = conexionProvider;
    }

    @Override
    public List<Telefono> listarPorPersona(int personaId) throws SQLException {
        String sql = "SELECT id, personaId, telefono FROM Telefonos WHERE personaId = ? ORDER BY id";
        List<Telefono> telefonos = new ArrayList<>();

        try (Connection conn = conexionProvider.obtenerConexion();
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

    @Override
    public int insertar(int personaId, String telefono) throws SQLException {
        String sql = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        try (Connection conn = conexionProvider.obtenerConexion();
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

    @Override
    public boolean actualizar(int id, String telefono) throws SQLException {
        String sql = "UPDATE Telefonos SET telefono = ? WHERE id = ?";

        try (Connection conn = conexionProvider.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, telefono);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Telefonos WHERE id = ?";

        try (Connection conn = conexionProvider.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
