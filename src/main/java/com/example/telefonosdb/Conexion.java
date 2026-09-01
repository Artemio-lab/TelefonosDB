package com.example.telefonosdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion implements ConexionProvider {

    private static final String URL_POR_DEFECTO = "jdbc:mariadb://localhost:3306/agenda";
    private static final String USER_POR_DEFECTO = "usuario1";
    private static final String PASSWORD_POR_DEFECTO = "superpassword";

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver JDBC de MariaDB", e);
        }
    }

    private final String url;
    private final String user;
    private final String password;

    /** Usa los datos de conexión por defecto. */
    public Conexion() {
        this(URL_POR_DEFECTO, USER_POR_DEFECTO, PASSWORD_POR_DEFECTO);
    }

    /** Permite apuntar a otra base sin tener que modificar esta clase. */
    public Conexion(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
