package com.example.telefonosdb;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConexionProvider {
    Connection obtenerConexion() throws SQLException;
}
