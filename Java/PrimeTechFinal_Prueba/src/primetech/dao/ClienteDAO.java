package primetech.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import primetech.db.ConexionDB;
import primetech.exception.ValidacionException;
import primetech.model.Cliente;
import primetech.security.ValidadorEntrada;
import primetech.util.AppLogger;

public class ClienteDAO {

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sqlP = "SELECT c.id_cliente, c.tipo, c.telefono, c.email, c.direccion, " +
                      "cp.nombre, cp.apellidos, cp.dni, NULL AS razon_social, NULL AS cif, NULL AS contacto_nombre " +
                      "FROM clientes c JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente";
        String sqlE = "SELECT c.id_cliente, c.tipo, c.telefono, c.email, c.direccion, " +
                      "NULL AS nombre, NULL AS apellidos, NULL AS dni, " +
                      "ce.razon_social, ce.cif, ce.contacto_nombre " +
                      "FROM clientes c JOIN clientes_empresa ce ON c.id_cliente = ce.id_cliente";
        try (Statement st = ConexionDB.getConexion().createStatement()) {
            try (ResultSet rs = st.executeQuery(sqlP)) {
                while (rs.next()) lista.add(mapear(rs));
            }
            try (ResultSet rs = st.executeQuery(sqlE)) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Cliente> listarParticulares() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.id_cliente, c.tipo, c.telefono, c.email, c.direccion, " +
                     "cp.nombre, cp.apellidos, cp.dni, NULL AS razon_social, NULL AS cif, NULL AS contacto_nombre " +
                     "FROM clientes c JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente ORDER BY cp.nombre";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Cliente> listarEmpresas() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.id_cliente, c.tipo, c.telefono, c.email, c.direccion, " +
                     "NULL AS nombre, NULL AS apellidos, NULL AS dni, " +
                     "ce.razon_social, ce.cif, ce.contacto_nombre " +
                     "FROM clientes c JOIN clientes_empresa ce ON c.id_cliente = ce.id_cliente ORDER BY ce.razon_social";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public void insertarParticular(Cliente c) throws SQLException, ValidacionException {
        ValidadorEntrada.validarNombre(c.getNombre(), "nombre");
        ValidadorEntrada.validarNombre(c.getApellidos(), "apellidos");
        ValidadorEntrada.validarDni(c.getDni());
        ValidadorEntrada.validarEmail(c.getEmail());
        ValidadorEntrada.validarTelefono(c.getTelefono());
        Connection conn = ConexionDB.getConexion();
        conn.setAutoCommit(false);
        try {
            int idCliente;
            String sqlC = "INSERT INTO clientes (tipo, telefono, email, direccion) VALUES ('particular',?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlC, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getTelefono());
                ps.setString(2, c.getEmail());
                ps.setString(3, c.getDireccion());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next(); idCliente = rs.getInt(1);
                }
            }
            String sqlP = "INSERT INTO clientes_particular (id_cliente, nombre, apellidos, dni) VALUES (?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlP)) {
                ps.setInt(1, idCliente);
                ps.setString(2, c.getNombre());
                ps.setString(3, c.getApellidos());
                ps.setString(4, c.getDni());
                ps.executeUpdate();
            }
            conn.commit();
            AppLogger.auditoria("Cliente particular insertado: " + c.getDni());
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void insertarEmpresa(Cliente c) throws SQLException, ValidacionException {
        ValidadorEntrada.validarNombre(c.getRazonSocial(), "razón social");
        ValidadorEntrada.validarCif(c.getCif());
        ValidadorEntrada.validarEmail(c.getEmail());
        ValidadorEntrada.validarTelefono(c.getTelefono());
        Connection conn = ConexionDB.getConexion();
        conn.setAutoCommit(false);
        try {
            int idCliente;
            String sqlC = "INSERT INTO clientes (tipo, telefono, email, direccion) VALUES ('empresa',?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlC, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getTelefono());
                ps.setString(2, c.getEmail());
                ps.setString(3, c.getDireccion());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next(); idCliente = rs.getInt(1);
                }
            }
            String sqlE = "INSERT INTO clientes_empresa (id_cliente, razon_social, cif, contacto_nombre) VALUES (?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlE)) {
                ps.setInt(1, idCliente);
                ps.setString(2, c.getRazonSocial());
                ps.setString(3, c.getCif());
                ps.setString(4, c.getContactoNombre());
                ps.executeUpdate();
            }
            conn.commit();
            AppLogger.auditoria("Cliente empresa insertado: " + c.getCif());
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void eliminar(int idCliente) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id_cliente=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
        AppLogger.auditoria("Cliente eliminado ID=" + idCliente);
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setTipo(rs.getString("tipo"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setDireccion(rs.getString("direccion"));
        if ("particular".equals(c.getTipo())) {
            c.setNombre(rs.getString("nombre"));
            c.setApellidos(rs.getString("apellidos"));
            c.setDni(rs.getString("dni"));
        } else {
            c.setRazonSocial(rs.getString("razon_social"));
            c.setCif(rs.getString("cif"));
            c.setContactoNombre(rs.getString("contacto_nombre"));
        }
        return c;
    }
}
