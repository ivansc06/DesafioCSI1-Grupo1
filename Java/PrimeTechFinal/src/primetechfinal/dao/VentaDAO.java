package primetechfinal.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import primetechfinal.db.ConexionDB;
import primetechfinal.model.DetalleVenta;
import primetechfinal.model.Venta;

public class VentaDAO {

    // logger para registrar todas las operaciones sobre ventas
    private static final Logger logger = LogManager.getLogger(VentaDAO.class);

    public int registrarVenta(Venta venta) throws SQLException {
        // con el pool hay que meter la conexion en el try-with-resources para que se cierre
        // al terminar y vuelva al pool, antes no era necesario porque era una conexion fija
        try (Connection conn = ConexionDB.getConexion()) {
            conn.setAutoCommit(false);
            try {
                int idVenta;
                String sqlV = "INSERT INTO ventas (id_cliente, id_empleado, metodo_pago, total) VALUES (?,?,?,0)";
                try (PreparedStatement ps = conn.prepareStatement(sqlV, Statement.RETURN_GENERATED_KEYS)) {
                    if (venta.getIdCliente() > 0) ps.setInt(1, venta.getIdCliente());
                    else ps.setNull(1, Types.INTEGER);
                    ps.setInt(2, venta.getIdEmpleado());
                    ps.setString(3, venta.getMetodoPago());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idVenta = rs.getInt(1);
                    }
                }
                String sqlD = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario) VALUES (?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlD)) {
                    for (DetalleVenta d : venta.getDetalles()) {
                        ps.setInt(1, idVenta);
                        ps.setInt(2, d.getIdProducto());
                        ps.setInt(3, d.getCantidad());
                        ps.setDouble(4, d.getPrecioUnitario());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
                // guardo la venta en el log con el id generado y el total
                logger.info("Venta registrada - id_venta: {}, id_empleado: {}, total: {}", idVenta, venta.getIdEmpleado(), venta.getTotal());
                return idVenta;
            } catch (SQLException e) {
                // si falla la transaccion lo registro como error grave
                logger.error("Error al registrar venta - id_empleado: {}: {}", venta.getIdEmpleado(), e.getMessage(), e);
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Venta> listarTodas() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT v.id_venta, v.id_cliente, v.id_empleado, v.fecha_venta, v.total, v.metodo_pago, " +
                     "COALESCE(cp.nombre, ce.razon_social, 'Sin cliente') AS nombre_cliente, c.email " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente " +
                     "LEFT JOIN clientes_empresa ce ON c.id_cliente = ce.id_cliente " +
                     "ORDER BY v.fecha_venta DESC";
        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Venta v = new Venta();
                v.setIdVenta(rs.getInt("id_venta"));
                v.setIdCliente(rs.getInt("id_cliente"));
                v.setIdEmpleado(rs.getInt("id_empleado"));
                Timestamp ts = rs.getTimestamp("fecha_venta");
                if (ts != null) v.setFechaVenta(ts.toLocalDateTime());
                v.setTotal(rs.getDouble("total"));
                v.setMetodoPago(rs.getString("metodo_pago"));
                v.setNombreCliente(rs.getString("nombre_cliente"));
                v.setEmailCliente(rs.getString("email"));
                lista.add(v);
            }
        }
        return lista;
    }

    public List<DetalleVenta> cargarDetalles(int idVenta) throws SQLException {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT dv.id_detalle, dv.id_venta, dv.id_producto, p.nombre, dv.cantidad, dv.precio_unitario " +
                     "FROM detalle_ventas dv JOIN productos p ON dv.id_producto = p.id_producto " +
                     "WHERE dv.id_venta = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setIdVenta(rs.getInt("id_venta"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("nombre"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    public Venta cargarVentaCompleta(int idVenta) throws SQLException {
        String sql = "SELECT v.id_venta, v.id_cliente, v.id_empleado, v.fecha_venta, v.total, v.metodo_pago, " +
                     "COALESCE(cp.nombre, ce.razon_social, 'Sin cliente') AS nombre_cliente, c.email " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente " +
                     "LEFT JOIN clientes_empresa ce ON c.id_cliente = ce.id_cliente " +
                     "WHERE v.id_venta = ?";
        Venta v = null;
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = new Venta();
                    v.setIdVenta(rs.getInt("id_venta"));
                    v.setIdCliente(rs.getInt("id_cliente"));
                    v.setIdEmpleado(rs.getInt("id_empleado"));
                    Timestamp ts = rs.getTimestamp("fecha_venta");
                    if (ts != null) v.setFechaVenta(ts.toLocalDateTime());
                    v.setTotal(rs.getDouble("total"));
                    v.setMetodoPago(rs.getString("metodo_pago"));
                    v.setNombreCliente(rs.getString("nombre_cliente"));
                    v.setEmailCliente(rs.getString("email"));
                    v.setDetalles(cargarDetalles(idVenta));
                }
            }
        }
        return v;
    }

    public List<Venta> buscarPorCliente(String nombre) throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT v.id_venta, v.id_cliente, v.id_empleado, v.fecha_venta, v.total, v.metodo_pago, " +
                     "COALESCE(cp.nombre, ce.razon_social, 'Sin cliente') AS nombre_cliente, c.email " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente " +
                     "LEFT JOIN clientes_empresa ce ON c.id_cliente = ce.id_cliente " +
                     "WHERE cp.nombre LIKE ? OR cp.apellidos LIKE ? OR ce.razon_social LIKE ? " +
                     "ORDER BY v.fecha_venta DESC";
        String filtro = "%" + nombre + "%";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.setIdVenta(rs.getInt("id_venta"));
                    v.setIdCliente(rs.getInt("id_cliente"));
                    v.setIdEmpleado(rs.getInt("id_empleado"));
                    Timestamp ts = rs.getTimestamp("fecha_venta");
                    if (ts != null) v.setFechaVenta(ts.toLocalDateTime());
                    v.setTotal(rs.getDouble("total"));
                    v.setMetodoPago(rs.getString("metodo_pago"));
                    v.setNombreCliente(rs.getString("nombre_cliente"));
                    v.setEmailCliente(rs.getString("email"));
                    lista.add(v);
                }
            }
        }
        return lista;
    }

    public void eliminar(int idVenta) throws SQLException {
        // NOTA: antes solo hacia DELETE FROM ventas y dejaba que MySQL borrase
        // los detalles automaticamente por el ON DELETE CASCADE de la clave foranea.
        // El problema es que cuando MySQL borra filas por CASCADE no dispara los triggers,
        // entonces el trigger que tenia en detalle_ventas para restaurar el stock
        // nunca se ejecutaba y el stock se quedaba mal despues de eliminar una venta.
        // Para solucionarlo hay que borrar los detalles a mano primero, asi el trigger
        // si se dispara y devuelve el stock correctamente, y luego ya se borra la venta.
        try (Connection conn = ConexionDB.getConexion()) {
            conn.setAutoCommit(false);
            try {
                // borramos primero los detalles para que el trigger de stock se active
                String sqlD = "DELETE FROM detalle_ventas WHERE id_venta=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlD)) {
                    ps.setInt(1, idVenta);
                    ps.executeUpdate();
                }
                // una vez restaurado el stock ya podemos borrar la venta
                String sqlV = "DELETE FROM ventas WHERE id_venta=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlV)) {
                    ps.setInt(1, idVenta);
                    ps.executeUpdate();
                }
                conn.commit();
                // uso warn porque eliminar una venta es algo que hay que dejar registrado siempre
                logger.warn("Venta eliminada - id_venta: {}", idVenta);
            } catch (SQLException e) {
                logger.error("Error al eliminar venta id={}: {}", idVenta, e.getMessage(), e);
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void actualizar(Venta venta) throws SQLException {
        try (Connection conn = ConexionDB.getConexion()) {
            conn.setAutoCommit(false);
            try {
                // actualizamos cliente y metodo de pago
                String sqlV = "UPDATE ventas SET id_cliente=?, metodo_pago=? WHERE id_venta=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlV)) {
                    if (venta.getIdCliente() > 0) ps.setInt(1, venta.getIdCliente());
                    else ps.setNull(1, Types.INTEGER);
                    ps.setString(2, venta.getMetodoPago());
                    ps.setInt(3, venta.getIdVenta());
                    ps.executeUpdate();
                }
                // eliminamos los detalles antiguos
                String sqlDelD = "DELETE FROM detalle_ventas WHERE id_venta=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlDelD)) {
                    ps.setInt(1, venta.getIdVenta());
                    ps.executeUpdate();
                }
                // insertamos los nuevos detalles
                String sqlD = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario) VALUES (?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlD)) {
                    for (DetalleVenta d : venta.getDetalles()) {
                        ps.setInt(1, venta.getIdVenta());
                        ps.setInt(2, d.getIdProducto());
                        ps.setInt(3, d.getCantidad());
                        ps.setDouble(4, d.getPrecioUnitario());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
                // registro que se ha modificado la venta correctamente
                logger.info("Venta actualizada - id_venta: {}", venta.getIdVenta());
            } catch (SQLException e) {
                // si falla durante la actualizacion lo registro y hago rollback
                logger.error("Error al actualizar venta id={}: {}", venta.getIdVenta(), e.getMessage(), e);
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public double totalVentasHoy() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) FROM ventas WHERE DATE(fecha_venta) = CURDATE()";
        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public int ventasMes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ventas WHERE MONTH(fecha_venta)=MONTH(CURDATE()) AND YEAR(fecha_venta)=YEAR(CURDATE())";
        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // devuelve el total de ventas agrupado por dia de los ultimos 7 dias
    // el mapa tiene como clave la fecha (dd/MM) y como valor el total vendido ese dia
    public java.util.LinkedHashMap<String, Double> ventasUltimos7Dias() throws SQLException {
        java.util.LinkedHashMap<String, Double> datos = new java.util.LinkedHashMap<>();
        String sql = "SELECT DATE(fecha_venta) AS dia, SUM(total) AS total " +
                     "FROM ventas " +
                     "WHERE fecha_venta >= CURDATE() - INTERVAL 6 DAY " +
                     "GROUP BY dia ORDER BY dia ASC";
        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                // formateamos la fecha como dd/MM para que quede mas corto en la grafica
                java.time.LocalDate fecha = rs.getDate("dia").toLocalDate();
                String etiqueta = String.format("%02d/%02d", fecha.getDayOfMonth(), fecha.getMonthValue());
                datos.put(etiqueta, rs.getDouble("total"));
            }
        }
        return datos;
    }

    // devuelve el top 5 de productos mas vendidos con su cantidad total vendida
    public java.util.LinkedHashMap<String, Integer> top5ProductosMasVendidos() throws SQLException {
        java.util.LinkedHashMap<String, Integer> datos = new java.util.LinkedHashMap<>();
        String sql = "SELECT p.nombre, SUM(dv.cantidad) AS total " +
                     "FROM detalle_ventas dv JOIN productos p ON dv.id_producto = p.id_producto " +
                     "GROUP BY p.id_producto ORDER BY total DESC LIMIT 5";
        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                datos.put(rs.getString("nombre"), rs.getInt("total"));
            }
        }
        return datos;
    }

    public String productoMasVendido() throws SQLException {
        String sql = "SELECT p.nombre, SUM(dv.cantidad) AS total " +
                     "FROM detalle_ventas dv JOIN productos p ON dv.id_producto=p.id_producto " +
                     "GROUP BY p.id_producto ORDER BY total DESC LIMIT 1";
        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString("nombre") : "-";
        }
    }
}
