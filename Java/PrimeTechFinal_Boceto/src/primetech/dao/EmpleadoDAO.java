package primetech.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import primetech.db.ConexionDB;
import primetech.exception.ValidacionException;
import primetech.model.Empleado;
import primetech.security.ValidadorEntrada;
import primetech.util.AppLogger;
import primetech.util.BCrypt;

public class EmpleadoDAO {

    public Empleado buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT id_empleado, nombre, apellidos, cargo, email, contraseña " +
                     "FROM empleados WHERE email = ?";
        try (PreparedStatement ps = ConexionDB.getConexionApp().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    /**
     * Autentica al empleado. Solo acepta contraseñas almacenadas con BCrypt.
     * Si se detecta una contraseña en texto plano, la migra automáticamente
     * y lanza un aviso en el log.
     */
    public Empleado login(String email, String plainPassword) throws SQLException {
        if (email == null || email.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty())
            return null;

        Empleado emp = buscarPorEmail(email.trim().toLowerCase());
        if (emp == null) return null;

        String stored = emp.getContraseña();
        boolean ok;

        if (esBCrypt(stored)) {
            ok = BCrypt.checkpw(plainPassword, stored);
        } else {
            // Contraseña en texto plano detectada: migrar a BCrypt
            AppLogger.warning("Contraseña en texto plano detectada para empleado ID=" +
                              emp.getIdEmpleado() + ". Migrando a BCrypt.");
            ok = stored.equals(plainPassword);
            if (ok) {
                String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
                actualizarContraseña(emp.getIdEmpleado(), hash);
                emp.setContraseña(hash);
            }
        }
        return ok ? emp : null;
    }

    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT id_empleado, nombre, apellidos, cargo, email, contraseña " +
                     "FROM empleados ORDER BY nombre";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * Inserta un empleado hasheando su contraseña con BCrypt.
     * Valida email y contraseña antes de persistir.
     */
    public void insertar(Empleado emp) throws SQLException, ValidacionException {
        ValidadorEntrada.validarEmail(emp.getEmail());
        ValidadorEntrada.validarNombre(emp.getNombre(), "nombre");
        ValidadorEntrada.validarNombre(emp.getApellidos(), "apellidos");
        ValidadorEntrada.validarContraseña(emp.getContraseña());

        String hash = BCrypt.hashpw(emp.getContraseña(), BCrypt.gensalt(12));
        String sql = "INSERT INTO empleados (nombre, apellidos, cargo, email, contraseña) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, emp.getNombre().trim());
            ps.setString(2, emp.getApellidos().trim());
            ps.setString(3, emp.getCargo());
            ps.setString(4, emp.getEmail().trim().toLowerCase());
            ps.setString(5, hash);
            ps.executeUpdate();
        }
        AppLogger.auditoria("Empleado insertado: " + emp.getEmail());
    }

    public void actualizar(Empleado emp) throws SQLException, ValidacionException {
        ValidadorEntrada.validarEmail(emp.getEmail());
        ValidadorEntrada.validarNombre(emp.getNombre(), "nombre");
        ValidadorEntrada.validarNombre(emp.getApellidos(), "apellidos");

        String sql = "UPDATE empleados SET nombre=?, apellidos=?, cargo=?, email=? WHERE id_empleado=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, emp.getNombre().trim());
            ps.setString(2, emp.getApellidos().trim());
            ps.setString(3, emp.getCargo());
            ps.setString(4, emp.getEmail().trim().toLowerCase());
            ps.setInt(5, emp.getIdEmpleado());
            ps.executeUpdate();
        }
        AppLogger.auditoria("Empleado actualizado ID=" + emp.getIdEmpleado());
    }

    /**
     * Actualiza la contraseña de un empleado.
     * Si se pasa texto plano, la hashea antes de guardar.
     * Si ya es un hash BCrypt ($2a$ o $2b$), la guarda directamente.
     */
    public void actualizarContraseña(int idEmpleado, String nuevaContraseña) throws SQLException {
        String hash = esBCrypt(nuevaContraseña)
            ? nuevaContraseña
            : BCrypt.hashpw(nuevaContraseña, BCrypt.gensalt(12));

        String sql = "UPDATE empleados SET contraseña=? WHERE id_empleado=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
        }
        AppLogger.auditoria("Contraseña actualizada para empleado ID=" + idEmpleado);
    }

    public void eliminar(int idEmpleado) throws SQLException {
        String sql = "DELETE FROM empleados WHERE id_empleado=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.executeUpdate();
        }
        AppLogger.auditoria("Empleado eliminado ID=" + idEmpleado);
    }

    private boolean esBCrypt(String hash) {
        return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$"));
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getInt("id_empleado"));
        e.setNombre(rs.getString("nombre"));
        e.setApellidos(rs.getString("apellidos"));
        e.setCargo(rs.getString("cargo"));
        e.setEmail(rs.getString("email"));
        e.setContraseña(rs.getString("contraseña"));
        return e;
    }
}
