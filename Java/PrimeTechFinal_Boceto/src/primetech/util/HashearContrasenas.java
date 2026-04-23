package primetech.util;

import primetech.db.ConexionDB;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Script de migración — ejecutar UNA SOLA VEZ si las contraseñas están en texto plano.
 * Lee todos los empleados cuya contraseña no empiece por "$2a$" y las hashea con BCrypt.
 */
public class HashearContrasenas {

    public static void main(String[] args) {
        try {
            migrarContrasenas();
            System.out.println("Proceso completado.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConexionDB.cerrar();
        }
    }

    public static void migrarContrasenas() throws SQLException {
        String selectSQL = "SELECT id_empleado, email, contraseña FROM empleados";
        String updateSQL = "UPDATE empleados SET contraseña = ? WHERE id_empleado = ?";

        Map<Integer, String[]> pendientes = new LinkedHashMap<>();

        try (Connection conn = ConexionDB.getConexionAdmin();
             PreparedStatement sel = conn.prepareStatement(selectSQL);
             ResultSet rs = sel.executeQuery()) {

            while (rs.next()) {
                int    id    = rs.getInt("id_empleado");
                String email = rs.getString("email");
                String pass  = rs.getString("contraseña");

                if (!pass.startsWith("$2a$") && !pass.startsWith("$2b$")) {
                    pendientes.put(id, new String[]{ email, pass });
                }
            }
        }

        if (pendientes.isEmpty()) {
            System.out.println("No hay contraseñas en texto plano. Nada que migrar.");
            return;
        }

        try (Connection conn = ConexionDB.getConexionAdmin();
             PreparedStatement upd = conn.prepareStatement(updateSQL)) {

            for (Map.Entry<Integer, String[]> entry : pendientes.entrySet()) {
                int    id    = entry.getKey();
                String email = entry.getValue()[0];
                String plain = entry.getValue()[1];

                String hash = BCrypt.hashpw(plain, BCrypt.gensalt());
                upd.setString(1, hash);
                upd.setInt(2, id);
                upd.executeUpdate();

                System.out.println("  [OK] " + email + " → hash guardado");
            }
        }
    }
}
