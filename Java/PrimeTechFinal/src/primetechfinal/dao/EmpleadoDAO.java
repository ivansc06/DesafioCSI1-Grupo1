package primetechfinal.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import primetechfinal.db.ConexionDB;
import primetechfinal.model.Empleado;
import primetechfinal.util.BCrypt;

public class EmpleadoDAO {

    public Empleado buscarPorEmail(String email) throws SQLException {
        // traigo tambien los campos de bloqueo para poder comprobarlos en el login
        String sql = "SELECT id_empleado, nombre, apellidos, cargo, email, contraseña, " +
                     "intentos_fallidos, bloqueado FROM empleados WHERE email = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public Empleado login(String email, String plainPassword) throws SQLException {
        Empleado emp = buscarPorEmail(email);

        // si el email no existe directamente devuelvo null
        if (emp == null) return null;

        // si la cuenta esta bloqueada no dejo ni intentar la contraseña
        if (emp.isBloqueado()) return null;

        String stored = emp.getContraseña();
        boolean ok;

        if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
            ok = BCrypt.checkpw(plainPassword, stored);
        } else {
            ok = stored.equals(plainPassword);
            if (ok) {
                String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                actualizarContraseña(emp.getIdEmpleado(), hash);
                emp.setContraseña(hash);
            }
        }

        if (ok) {
            // contraseña correcta, reseteo el contador para que pueda volver a fallar desde 0
            try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(
                    "UPDATE empleados SET intentos_fallidos = 0, bloqueado = 0 WHERE id_empleado = ?")) {
                ps.setInt(1, emp.getIdEmpleado());
                ps.executeUpdate();
            }
        } else {
            // contraseña incorrecta, sumo 1 al contador
            // el trigger trg_bloquear_cuenta en la BD se encarga de poner bloqueado = 1 si llega a 5
            try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(
                    "UPDATE empleados SET intentos_fallidos = intentos_fallidos + 1 WHERE id_empleado = ?")) {
                ps.setInt(1, emp.getIdEmpleado());
                ps.executeUpdate();
            }
        }

        return ok ? emp : null;
    }

    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT id_empleado, nombre, apellidos, cargo, email, contraseña, " +
                     "intentos_fallidos, bloqueado FROM empleados ORDER BY nombre";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public void insertar(Empleado emp) throws SQLException {
        String hash = BCrypt.hashpw(emp.getContraseña(), BCrypt.gensalt());
        String sql = "INSERT INTO empleados (nombre, apellidos, cargo, email, contraseña) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getApellidos());
            ps.setString(3, emp.getCargo());
            ps.setString(4, emp.getEmail());
            ps.setString(5, hash);
            ps.executeUpdate();
        }
    }

    public void actualizar(Empleado emp) throws SQLException {
        String sql = "UPDATE empleados SET nombre=?, apellidos=?, cargo=?, email=? WHERE id_empleado=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getApellidos());
            ps.setString(3, emp.getCargo());
            ps.setString(4, emp.getEmail());
            ps.setInt(5, emp.getIdEmpleado());
            ps.executeUpdate();
        }
    }

    public void actualizarContraseña(int idEmpleado, String nuevaContraseñaPlain) throws SQLException {
        String hash = nuevaContraseñaPlain.startsWith("$2a$")
            ? nuevaContraseñaPlain
            : BCrypt.hashpw(nuevaContraseñaPlain, BCrypt.gensalt());
        String sql = "UPDATE empleados SET contraseña=? WHERE id_empleado=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idEmpleado) throws SQLException {
        String sql = "DELETE FROM empleados WHERE id_empleado=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.executeUpdate();
        }
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getInt("id_empleado"));
        e.setNombre(rs.getString("nombre"));
        e.setApellidos(rs.getString("apellidos"));
        e.setCargo(rs.getString("cargo"));
        e.setEmail(rs.getString("email"));
        e.setContraseña(rs.getString("contraseña"));
        // mapeo los campos nuevos de bloqueo
        e.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        e.setBloqueado(rs.getBoolean("bloqueado"));
        return e;
    }
}
