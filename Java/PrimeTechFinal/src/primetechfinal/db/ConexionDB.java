package primetechfinal.db;

import primetechfinal.sesion.Sesion;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class ConexionDB {

    // cargo la configuracion desde el archivo config.properties en lugar de tenerla hardcodeada aqui
    private static final String URL;
    private static final String USUARIO_APP;
    private static final String CLAVE_APP;
    private static final String USUARIO_ADMIN;
    private static final String CLAVE_ADMIN;

    // bloque estatico que se ejecuta una sola vez cuando se carga la clase
    // si el archivo no existe o falta alguna clave, lanza un error al arrancar la app
    static {
        Properties props = new Properties();
        try (InputStream is = ConexionDB.class.getResourceAsStream("/primetechfinal/config.properties")) {
            if (is == null) {
                throw new RuntimeException("No se encontro el archivo config.properties en el classpath.");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer config.properties: " + e.getMessage());
        }

        URL           = props.getProperty("db.url");
        USUARIO_APP   = props.getProperty("db.usuario.app");
        CLAVE_APP     = props.getProperty("db.clave.app");
        USUARIO_ADMIN = props.getProperty("db.usuario.admin");
        CLAVE_ADMIN   = props.getProperty("db.clave.admin");
    }

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
