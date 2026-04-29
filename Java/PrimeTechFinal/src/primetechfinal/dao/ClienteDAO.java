package primetechfinal.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import primetechfinal.db.ConexionDB;
import primetechfinal.model.Cliente;

public class ClienteDAO {

    // logger para dejar registro de lo que se hace con los clientes
    private static final Logger logger = LogManager.getLogger(ClienteDAO.class);

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sqlP = "SELECT c.id_cliente, c.tipo, c.telefono, c.email, c.direccion, " +//clientes particulares
                      "cp.nombre, cp.apellidos, cp.dni, NULL AS razon_social, NULL AS cif, NULL AS contacto_nombre " +
                      "FROM clientes c JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente";
        
        String sqlE = "SELECT c.id_cliente, c.tipo, c.telefono, c.email, c.direccion, " +//empresas
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

    public void insertarParticular(Cliente c) throws SQLException {
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
            // si llega aqui el commit fue bien, dejo constancia del nuevo cliente
            logger.info("Cliente particular creado: {} {}", c.getNombre(), c.getApellidos());
        } catch (SQLException e) {
            // si falla hago rollback y lo registro como error
            logger.error("Error al crear cliente particular: {}", e.getMessage(), e);
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void insertarEmpresa(Cliente c) throws SQLException {
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
            // si llega aqui el commit fue bien, dejo constancia de la nueva empresa
            logger.info("Cliente empresa creado: {}", c.getRazonSocial());
        } catch (SQLException e) {
            // si falla hago rollback y lo registro como error
            logger.error("Error al crear cliente empresa: {}", e.getMessage(), e);
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
        // uso warn porque eliminar un cliente es una accion que no se puede deshacer
        logger.warn("Cliente eliminado - id_cliente: {}", idCliente);
    }

    public List<Cliente> buscarPorNombre(String nombre) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        // busca en particulares por nombre o apellidos
        String sqlP = "SELECT c.id_cliente, c.tipo, c.telefono, c.email, c.direccion, " +
                      "cp.nombre, cp.apellidos, cp.dni, NULL AS razon_social, NULL AS cif, NULL AS contacto_nombre " +
                      "FROM clientes c JOIN clientes_particular cp ON c.id_cliente = cp.id_cliente " +
                      "WHERE cp.nombre LIKE ? OR cp.apellidos LIKE ?";
        // busca en empresas por razon social
        String sqlE = "SELECT c.id_cliente, c.tipo, c.telefono, c.email, c.direccion, " +
                      "NULL AS nombre, NULL AS apellidos, NULL AS dni, " +
                      "ce.razon_social, ce.cif, ce.contacto_nombre " +
                      "FROM clientes c JOIN clientes_empresa ce ON c.id_cliente = ce.id_cliente " +
                      "WHERE ce.razon_social LIKE ?";
        String filtro = "%" + nombre + "%";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sqlP)) {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sqlE)) {
            ps.setString(1, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void actualizarParticular(Cliente c) throws SQLException {
        Connection conn = ConexionDB.getConexion();
        conn.setAutoCommit(false);
        try {
            // actualizamos la tabla base clientes
            String sqlC = "UPDATE clientes SET telefono=?, email=?, direccion=? WHERE id_cliente=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlC)) {
                ps.setString(1, c.getTelefono());
                ps.setString(2, c.getEmail());
                ps.setString(3, c.getDireccion());
                ps.setInt(4, c.getIdCliente());
                ps.executeUpdate();
            }
            // actualizamos los datos específicos del particular
            String sqlP = "UPDATE clientes_particular SET nombre=?, apellidos=?, dni=? WHERE id_cliente=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlP)) {
                ps.setString(1, c.getNombre());
                ps.setString(2, c.getApellidos());
                ps.setString(3, c.getDni());
                ps.setInt(4, c.getIdCliente());
                ps.executeUpdate();
            }
            conn.commit();
            // registro la modificacion del cliente particular
            logger.info("Cliente particular actualizado: id={}", c.getIdCliente());
        } catch (SQLException e) {
            // si algo falla durante el update lo registro y hago rollback
            logger.error("Error al actualizar cliente particular id={}: {}", c.getIdCliente(), e.getMessage(), e);
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void actualizarEmpresa(Cliente c) throws SQLException {
        Connection conn = ConexionDB.getConexion();
        conn.setAutoCommit(false);
        try {
            // actualizamos la tabla base clientes
            String sqlC = "UPDATE clientes SET telefono=?, email=?, direccion=? WHERE id_cliente=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlC)) {
                ps.setString(1, c.getTelefono());
                ps.setString(2, c.getEmail());
                ps.setString(3, c.getDireccion());
                ps.setInt(4, c.getIdCliente());
                ps.executeUpdate();
            }
            // actualizamos los datos específicos de la empresa
            String sqlE = "UPDATE clientes_empresa SET razon_social=?, cif=?, contacto_nombre=? WHERE id_cliente=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlE)) {
                ps.setString(1, c.getRazonSocial());
                ps.setString(2, c.getCif());
                ps.setString(3, c.getContactoNombre());
                ps.setInt(4, c.getIdCliente());
                ps.executeUpdate();
            }
            conn.commit();
            // registro la modificacion de la empresa
            logger.info("Cliente empresa actualizado: id={}", c.getIdCliente());
        } catch (SQLException e) {
            // si algo falla durante el update lo registro y hago rollback
            logger.error("Error al actualizar cliente empresa id={}: {}", c.getIdCliente(), e.getMessage(), e);
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
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
