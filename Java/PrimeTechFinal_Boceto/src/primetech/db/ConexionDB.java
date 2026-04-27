package primetech.db;

import primetech.config.ConfiguracionApp;
import primetech.sesion.Sesion;
import primetech.util.AppLogger;
import java.sql.*;

/**
 * Gestiona las conexiones a la base de datos.
 * Las credenciales se cargan desde config.properties, nunca desde el código.
 */
public class ConexionDB {

    private static Connection conexionApp   = null;
    private static Connection conexionAdmin = null;

    private ConexionDB() {}

    /**
     * Devuelve la conexión adecuada según el rol del empleado en sesión.
     * Los DAOs siempre usan este método.
     */
    public static Connection getConexion() throws SQLException {
        if (Sesion.haySession() && Sesion.esAdmin()) {
            return getConexionAdmin();
        }
        return getConexionApp();
    }

    /**
     * Conexión con usuario de aplicación (permisos limitados).
     * Usada también durante el login, antes de conocer el rol.
     */
    public static Connection getConexionApp() throws SQLException {
        try {
            if (conexionApp == null || conexionApp.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String usuario = ConfiguracionApp.getDbUsuarioApp();
                String clave   = ConfiguracionApp.getDbClaveApp();
                if (usuario.isEmpty())
                    throw new SQLException("Credenciales de BD no configuradas. Revisa config.properties.");
                conexionApp = DriverManager.getConnection(ConfiguracionApp.getDbUrl(), usuario, clave);
                AppLogger.info("Conexión app establecida.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado: " + e.getMessage());
        }
        return conexionApp;
    }

    /**
     * Conexión con usuario administrador (permisos completos).
     * Solo accesible para empleados con cargo "admin".
     */
    public static Connection getConexionAdmin() throws SQLException {
        try {
            if (conexionAdmin == null || conexionAdmin.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String usuario = ConfiguracionApp.getDbUsuarioAdmin();
                String clave   = ConfiguracionApp.getDbClaveAdmin();
                if (usuario.isEmpty())
                    throw new SQLException("Credenciales de admin no configuradas. Revisa config.properties.");
                conexionAdmin = DriverManager.getConnection(ConfiguracionApp.getDbUrl(), usuario, clave);
                AppLogger.info("Conexión admin establecida.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado: " + e.getMessage());
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
            AppLogger.warning("Error al cerrar conexión: " + e.getMessage());
        }
    }
}
