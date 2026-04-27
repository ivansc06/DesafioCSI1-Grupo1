package primetech.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import primetech.db.ConexionDB;
import primetech.exception.ValidacionException;
import primetech.model.Producto;
import primetech.security.ValidadorEntrada;
import primetech.util.AppLogger;

public class ProductoDAO {

    public List<Producto> listarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, nombre, descripcion, precio_compra, precio_venta, stock " +
                     "FROM productos ORDER BY nombre";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Producto> buscarPorNombre(String nombre) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, nombre, descripcion, precio_compra, precio_venta, stock " +
                     "FROM productos WHERE nombre LIKE ? ESCAPE '\\\\' ORDER BY nombre";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, "%" + ValidadorEntrada.escaparLike(nombre) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Producto buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_producto, nombre, descripcion, precio_compra, precio_venta, stock " +
                     "FROM productos WHERE id_producto=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public void insertar(Producto p) throws SQLException, ValidacionException {
        ValidadorEntrada.validarNombre(p.getNombre(), "nombre");
        ValidadorEntrada.validarPrecio(p.getPrecioCompra(), "precio de compra");
        ValidadorEntrada.validarPrecio(p.getPrecioVenta(), "precio de venta");
        ValidadorEntrada.validarStock(p.getStock());

        String sql = "INSERT INTO productos (nombre, descripcion, precio_compra, precio_venta, stock) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, p.getNombre().trim());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecioCompra());
            ps.setDouble(4, p.getPrecioVenta());
            ps.setInt(5, p.getStock());
            ps.executeUpdate();
        }
        AppLogger.auditoria("Producto insertado: " + p.getNombre());
    }

    public void actualizar(Producto p) throws SQLException, ValidacionException {
        ValidadorEntrada.validarNombre(p.getNombre(), "nombre");
        ValidadorEntrada.validarPrecio(p.getPrecioCompra(), "precio de compra");
        ValidadorEntrada.validarPrecio(p.getPrecioVenta(), "precio de venta");
        ValidadorEntrada.validarStock(p.getStock());

        String sql = "UPDATE productos SET nombre=?, descripcion=?, precio_compra=?, precio_venta=?, stock=? WHERE id_producto=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, p.getNombre().trim());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecioCompra());
            ps.setDouble(4, p.getPrecioVenta());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getIdProducto());
            ps.executeUpdate();
        }
        AppLogger.auditoria("Producto actualizado ID=" + p.getIdProducto());
    }

    public void eliminar(int idProducto) throws SQLException {
        String sql = "DELETE FROM productos WHERE id_producto=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.executeUpdate();
        }
        AppLogger.auditoria("Producto eliminado ID=" + idProducto);
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
