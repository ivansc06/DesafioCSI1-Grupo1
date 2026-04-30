package primetechfinal.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import primetechfinal.sesion.Sesion;

public class ConexionDB {

    private static HikariDataSource poolApp;
    private static HikariDataSource poolAdmin;

    // al arrancar la clase creamos los dos pools con la config del properties
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

        // pool para el usuario de la app (vendedores, tecnicos, gerentes)
        HikariConfig cfgApp = new HikariConfig();
        cfgApp.setJdbcUrl(props.getProperty("db.url"));
        cfgApp.setUsername(props.getProperty("db.usuario.app"));
        cfgApp.setPassword(props.getProperty("db.clave.app"));
        cfgApp.setMaximumPoolSize(5);
        cfgApp.setMinimumIdle(0); // no mantenemos conexiones abiertas si no hay actividad
        cfgApp.setConnectionTimeout(30000);
        poolApp = new HikariDataSource(cfgApp);

        // pool para el administrador
        HikariConfig cfgAdmin = new HikariConfig();
        cfgAdmin.setJdbcUrl(props.getProperty("db.url"));
        cfgAdmin.setUsername(props.getProperty("db.usuario.admin"));
        cfgAdmin.setPassword(props.getProperty("db.clave.admin"));
        cfgAdmin.setMaximumPoolSize(3);
        cfgAdmin.setMinimumIdle(0); // igual que el de app, solo abre conexiones cuando se necesitan
        cfgAdmin.setConnectionTimeout(30000);
        poolAdmin = new HikariDataSource(cfgAdmin);
    }

    private ConexionDB() {}

    // devuelve una conexion del pool segun el cargo del empleado en sesion
    public static Connection getConexion() throws SQLException {
        if (Sesion.haySession()) {
            String cargo = Sesion.getEmpleado().getCargo();
            if ("admin".equals(cargo)) {
                return getConexionAdmin();
            }
        }
        return getConexionApp();
    }

    // para el login, antes de saber el cargo
    public static Connection getConexionApp() throws SQLException {
        return poolApp.getConnection();
    }

    // necesario que sea public para que pueda acceder la clase HashearContrasenas
    public static Connection getConexionAdmin() throws SQLException {
        return poolAdmin.getConnection();
    }

    // se llama al cerrar la aplicacion para liberar todos los recursos del pool
    public static void cerrar() {
        if (poolApp   != null) poolApp.close();
        if (poolAdmin != null) poolAdmin.close();
    }
}
