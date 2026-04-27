package primetechfinal.db;

import primetechfinal.sesion.Sesion;
import java.sql.*;

public class ConexionDB {

    private static final String URL =
        "jdbc:mysql://localhost:3306/tienda_informatica" +
        "?useSSL=false&serverTimezone=Europe/Madrid&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";

    private static final String USUARIO_APP   = "tienda_app";
    private static final String CLAVE_APP     = "app_pass";

    private static final String USUARIO_ADMIN = "tienda_admin";
    private static final String CLAVE_ADMIN   = "admin_pass";

    private static Connection conexionApp   = null;
    private static Connection conexionAdmin = null;

    private ConexionDB() {}

    /**
     * Devuelve la conexión según el cargo del empleado en sesión.
     * Los DAOs siempre llaman a este método.
     */
    public static Connection getConexion() throws SQLException {
        if (Sesion.haySession()) {
            String cargo = Sesion.getEmpleado().getCargo();
            if ("admin".equals(cargo)) {
                return getConexionAdmin();
            }
        }
        return getConexionApp();
    }

    /**
     * Solo para el proceso de login, antes de conocer el cargo.
     */
    public static Connection getConexionApp() throws SQLException {
        try {
            if (conexionApp == null || conexionApp.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexionApp = DriverManager.getConnection(URL, USUARIO_APP, CLAVE_APP);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado.\n" + e.getMessage());
        }
        return conexionApp;
    }

    public static Connection getConexionAdmin() throws SQLException {//necesario que sea public para que pueda acceder la clase hashearcontraseña
        try {
            if (conexionAdmin == null || conexionAdmin.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexionAdmin = DriverManager.getConnection(URL, USUARIO_ADMIN, CLAVE_ADMIN);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado.\n" + e.getMessage());
        }
        return conexionAdmin;
    }

    public static void cerrar() {
        cerrarConexion(conexionApp);
        cerrarConexion(conexionAdmin);
        conexionApp   = null;
        conexionAdmin = null;
    }

    private static void cerrarConexion(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}