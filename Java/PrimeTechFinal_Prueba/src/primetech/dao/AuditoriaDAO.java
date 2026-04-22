package primetech.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import primetech.db.ConexionDB;
import primetech.util.AppLogger;

/**
 * Registra en la tabla 'auditoria' todas las operaciones relevantes
 * (inserciones, modificaciones, eliminaciones y accesos).
 *
 * La tabla se crea automáticamente si no existe (requiere permisos CREATE TABLE).
 * Script equivalente disponible en sql/auditoria.sql.
 */
public class AuditoriaDAO {

    public static final String ACCION_LOGIN    = "LOGIN";
    public static final String ACCION_LOGOUT   = "LOGOUT";
    public static final String ACCION_INSERTAR = "INSERT";
    public static final String ACCION_ACTUALIZAR = "UPDATE";
    public static final String ACCION_ELIMINAR = "DELETE";

    public AuditoriaDAO() {
        crearTablasSiNoExisten();
    }

    /**
     * Registra una acción de auditoría. Silencia errores para no interrumpir
     * el flujo principal si la tabla de auditoría falla.
     *
     * @param idEmpleado  ID del empleado que realiza la acción (0 si no autenticado)
     * @param accion      Tipo de acción (usar las constantes de esta clase)
     * @param tablaAfectada Tabla sobre la que se actúa
     * @param descripcion Descripción legible de la operación
     */
    public void registrar(int idEmpleado, String accion, String tablaAfectada, String descripcion) {
        String sql = "INSERT INTO auditoria (id_empleado, accion, tabla_afectada, descripcion) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionDB.getConexionApp().prepareStatement(sql)) {
            if (idEmpleado > 0) {
                ps.setInt(1, idEmpleado);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, accion);
            ps.setString(3, tablaAfectada);
            ps.setString(4, descripcion);
            ps.executeUpdate();
        } catch (SQLException e) {
            AppLogger.warning("No se pudo registrar auditoría [" + accion + "]: " + e.getMessage());
        }

        AppLogger.auditoria("emp=" + idEmpleado + " | " + accion + " | " + tablaAfectada + " | " + descripcion);
    }

    /**
     * Devuelve los últimos N registros de auditoría, ordenados por fecha descendente.
     */
    public List<String[]> listarUltimos(int limite) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT a.fecha, e.nombre, e.apellidos, a.accion, a.tabla_afectada, a.descripcion " +
                     "FROM auditoria a LEFT JOIN empleados e ON a.id_empleado = e.id_empleado " +
                     "ORDER BY a.fecha DESC LIMIT ?";
        try (PreparedStatement ps = ConexionDB.getConexionApp().prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha");
                    String fecha = ts != null ? ts.toLocalDateTime().toString() : "-";
                    String empleado = rs.getString("nombre") != null
                        ? rs.getString("nombre") + " " + rs.getString("apellidos")
                        : "Sistema";
                    lista.add(new String[]{
                        fecha,
                        empleado,
                        rs.getString("accion"),
                        rs.getString("tabla_afectada"),
                        rs.getString("descripcion")
                    });
                }
            }
        }
        return lista;
    }

    private void crearTablasSiNoExisten() {
        String sql =
            "CREATE TABLE IF NOT EXISTS auditoria (" +
            "  id_auditoria   INT AUTO_INCREMENT PRIMARY KEY, " +
            "  id_empleado    INT NULL, " +
            "  accion         VARCHAR(20) NOT NULL, " +
            "  tabla_afectada VARCHAR(50), " +
            "  descripcion    TEXT, " +
            "  fecha          TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "  FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado) ON DELETE SET NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement st = ConexionDB.getConexionApp().createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            AppLogger.warning("No se pudo crear la tabla de auditoría: " + e.getMessage());
        }
    }
}
