package primetechfinal.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import primetechfinal.db.ConexionDB;
import primetechfinal.model.Producto;

public class ProductoDAO {

    // logger para registrar los cambios que se hacen en los productos
    private static final Logger logger = LogManager.getLogger(ProductoDAO.class);

    public List<Producto> listarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, nombre, descripcion, precio_compra, precio_venta, stock " +
                     "FROM productos ORDER BY nombre";
        // con el pool hay que cerrar la conexion al terminar para que vuelva al pool
        // antes era una conexion unica y no hacia falta cerrarla
        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Producto> buscarPorNombre(String nombre) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, nombre, descripcion, precio_compra, precio_venta, stock " +
                     "FROM productos WHERE nombre LIKE ? ORDER BY nombre";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Producto buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_producto, nombre, descripcion, precio_compra, precio_venta, stock " +
                     "FROM productos WHERE id_producto=?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (nombre, descripcion, precio_compra, precio_venta, stock) VALUES (?,?,?,?,?)";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecioCompra());
            ps.setDouble(4, p.getPrecioVenta());
            ps.setInt(5, p.getStock());
            ps.executeUpdate();
        }
        // guardo en el log que se ha añadido un producto nuevo con su precio
        logger.info("Producto creado: {} - precio venta: {}", p.getNombre(), p.getPrecioVenta());
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE productos SET nombre=?, descripcion=?, precio_compra=?, precio_venta=?, stock=? WHERE id_producto=?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecioCompra());
            ps.setDouble(4, p.getPrecioVenta());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getIdProducto());
            ps.executeUpdate();
        }
        // registro que se ha modificado un producto
        logger.info("Producto actualizado: id={}, nombre={}", p.getIdProducto(), p.getNombre());
    }

    public void eliminar(int idProducto) throws SQLException {
        String sql = "DELETE FROM productos WHERE id_producto=?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.executeUpdate();
        }
        // warn porque si se elimina un producto con ventas asociadas podria ser un problema
        logger.warn("Producto eliminado - id_producto: {}", idProducto);
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
            rs.getInt("id_producto"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getDouble("precio_compra"),
            rs.getDouble("precio_venta"),
            rs.getInt("stock")
        );
    }
}
