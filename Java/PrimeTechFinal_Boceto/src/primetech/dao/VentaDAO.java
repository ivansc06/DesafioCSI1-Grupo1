package primetech.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import primetech.db.ConexionDB;
import primetech.model.DetalleVenta;
import primetech.model.Venta;
import primetech.util.AppLogger;

public class VentaDAO {

    /**
     * Registra una venta completa en una transacción:
     * 1. Inserta la cabecera de la venta con el total calculado.
     * 2. Inserta cada línea de detalle.
     * 3. Descuenta el stock de cada producto.
     *
     * @return ID de la venta generada
     * @throws IllegalArgumentException si la venta no tiene detalles
     */
    public int registrarVenta(Venta venta) throws SQLException {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty())
            throw new IllegalArgumentException("La venta debe tener al menos un producto.");

        double totalCalculado = venta.getDetalles().stream()
            .mapToDouble(DetalleVenta::getSubtotal)
            .sum();

        Connection conn = ConexionDB.getConexion();
        conn.setAutoCommit(false);
        try {
            int idVenta = insertarCabeceraVenta(conn, venta, totalCalculado);
            insertarDetalles(conn, idVenta, venta.getDetalles());
            actualizarStock(conn, venta.getDetalles());
            conn.commit();
            venta.setTotal(totalCalculado);
            AppLogger.auditoria("Venta registrada ID=" + idVenta + " total=" + totalCalculado);
            return idVenta;
        } catch (SQLException e) {
            conn.rollback();
            AppLogger.error("Error al registrar venta, rollback ejecutado.", e);
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Venta> listarTodas() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql =
            "SELECT v.id_venta, v.id_cliente, v.id_empleado, v.fecha_venta, v.total, v.metodo_pago, " +
            "COALESCE(cp.nombre, ce.razon_social, 'Sin cliente') AS nombre_cliente " +
            "FROM ventas v " +
            "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
            "LEFT JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente " +
            "LEFT JOIN clientes_empresa ce ON c.id_cliente = ce.id_cliente " +
            "ORDER BY v.fecha_venta DESC";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapearVenta(rs));
        }
        return lista;
    }

    public List<DetalleVenta> cargarDetalles(int idVenta) throws SQLException {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql =
            "SELECT dv.id_detalle, dv.id_venta, dv.id_producto, p.nombre, " +
            "dv.cantidad, dv.precio_unitario " +
            "FROM detalle_ventas dv JOIN productos p ON dv.id_producto = p.id_producto " +
            "WHERE dv.id_venta = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearDetalle(rs));
            }
        }
        return lista;
    }

    public Venta cargarVentaCompleta(int idVenta) throws SQLException {
        String sql =
            "SELECT v.id_venta, v.id_cliente, v.id_empleado, v.fecha_venta, v.total, v.metodo_pago, " +
            "COALESCE(cp.nombre, ce.razon_social, 'Sin cliente') AS nombre_cliente " +
            "FROM ventas v " +
            "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
            "LEFT JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente " +
            "LEFT JOIN clientes_empresa ce ON c.id_cliente = ce.id_cliente " +
            "WHERE v.id_venta = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Venta v = mapearVenta(rs);
                    v.setDetalles(cargarDetalles(idVenta));
                    return v;
                }
            }
        }
        return null;
    }

    public double totalVentasHoy() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha_venta) = CURDATE()";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public int ventasMes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ventas " +
                     "WHERE MONTH(fecha_venta) = MONTH(CURDATE()) AND YEAR(fecha_venta) = YEAR(CURDATE())";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public String productoMasVendido() throws SQLException {
        String sql =
            "SELECT p.nombre, SUM(dv.cantidad) AS total " +
            "FROM detalle_ventas dv JOIN productos p ON dv.id_producto = p.id_producto " +
            "GROUP BY p.id_producto ORDER BY total DESC LIMIT 1";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString("nombre") : "-";
        }
    }

    // ── Métodos privados ─────────────────────────────────────────────────────

    private int insertarCabeceraVenta(Connection conn, Venta venta, double total) throws SQLException {
        String sql = "INSERT INTO ventas (id_cliente, id_empleado, metodo_pago, total) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (venta.getIdCliente() > 0) ps.setInt(1, venta.getIdCliente());
            else                          ps.setNull(1, Types.INTEGER);
            ps.setInt(2, venta.getIdEmpleado());
            ps.setString(3, venta.getMetodoPago());
            ps.setDouble(4, total);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void insertarDetalles(Connection conn, int idVenta, List<DetalleVenta> detalles) throws SQLException {
        String sql = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario) " +
                     "VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DetalleVenta d : detalles) {
                ps.setInt(1, idVenta);
                ps.setInt(2, d.getIdProducto());
                ps.setInt(3, d.getCantidad());
                ps.setDouble(4, d.getPrecioUnitario());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void actualizarStock(Connection conn, List<DetalleVenta> detalles) throws SQLException {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DetalleVenta d : detalles) {
                ps.setInt(1, d.getCantidad());
                ps.setInt(2, d.getIdProducto());
                ps.setInt(3, d.getCantidad());
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException(
                        "Stock insuficiente para el producto ID=" + d.getIdProducto() +
                        " (solicitado: " + d.getCantidad() + ").");
                }
            }
        }
    }

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        v.setIdCliente(rs.getInt("id_cliente"));
        v.setIdEmpleado(rs.getInt("id_empleado"));
        Timestamp ts = rs.getTimestamp("fecha_venta");
        if (ts != null) v.setFechaVenta(ts.toLocalDateTime());
        v.setTotal(rs.getDouble("total"));
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setNombreCliente(rs.getString("nombre_cliente"));
        return v;
    }

    private DetalleVenta mapearDetalle(ResultSet rs) throws SQLException {
        DetalleVenta d = new DetalleVenta();
        d.setIdDetalle(rs.getInt("id_detalle"));
        d.setIdVenta(rs.getInt("id_venta"));
        d.setIdProducto(rs.getInt("id_producto"));
        d.setNombreProducto(rs.getString("nombre"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setPrecioUnitario(rs.getDouble("precio_unitario"));
        return d;
    }
}
