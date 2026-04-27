package primetech.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carga la configuración desde config.properties en el classpath.
 * Elimina las credenciales hardcodeadas del código fuente.
 */
public class ConfiguracionApp {

    private static final Logger LOG = Logger.getLogger(ConfiguracionApp.class.getName());
    private static final String ARCHIVO = "config.properties";
    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfiguracionApp.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (is != null) {
                props.load(is);
            } else {
                LOG.severe("No se encontró " + ARCHIVO + " en el classpath. " +
                           "La aplicación no podrá conectarse a la base de datos.");
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error al cargar " + ARCHIVO, e);
        }
    }

    private ConfiguracionApp() {}

    public static String get(String clave) {
        return props.getProperty(clave);
    }

    public static String get(String clave, String valorPorDefecto) {
        return props.getProperty(clave, valorPorDefecto);
    }

    // ── Base de datos ────────────────────────────────────────────────────────

    public static String getDbUrl() {
        return get("db.url",
            "jdbc:mysql://localhost:3306/tienda_informatica" +
            "?useSSL=true&serverTimezone=Europe/Madrid" +
            "&allowPublicKeyRetrieval=true&characterEncoding=UTF-8");
    }

    public static String getDbUsuarioApp() {
        return get("db.usuario.app", "");
    }

    public static String getDbClaveApp() {
        return get("db.clave.app", "");
    }

    public static String getDbUsuarioAdmin() {
        return get("db.usuario.admin", "");
    }

    public static String getDbClaveAdmin() {
        return get("db.clave.admin", "");
    }

    // ── Seguridad de login ───────────────────────────────────────────────────

    public static int getMaxIntentosFallidos() {
        try {
            return Integer.parseInt(get("seguridad.max.intentos", "5"));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public static int getTiempoBloqueoMinutos() {
        try {
            return Integer.parseInt(get("seguridad.bloqueo.minutos", "15"));
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    // ── Sesión ───────────────────────────────────────────────────────────────

    public static int getTimeoutSesionMinutos() {
        try {
            return Integer.parseInt(get("sesion.timeout.minutos", "30"));
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    // ── Datos de empresa (facturas) ──────────────────────────────────────────

    public static String getEmpresaNombre() {
        return get("empresa.nombre", "Prime Tech Systems");
    }

    public static String getEmpresaCif() {
        return get("empresa.cif", "");
    }

    public static String getEmpresaEmail() {
        return get("empresa.email", "");
    }

    public static String getEmpresaTelefono() {
        return get("empresa.telefono", "");
    }

    public static String getEmpresaDireccion() {
        return get("empresa.direccion", "");
    }

    public static double getIvaPorc() {
        try {
            return Double.parseDouble(get("empresa.iva", "0.21"));
        } catch (NumberFormatException e) {
            return 0.21;
        }
    }
}
