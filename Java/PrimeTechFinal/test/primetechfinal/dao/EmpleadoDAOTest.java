package primetechfinal.dao;

import java.sql.SQLException;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import primetechfinal.model.Empleado;

/**
 * Tests de integracion para EmpleadoDAO.
 * Comprueban que las operaciones con la base de datos funcionan correctamente.
 */
public class EmpleadoDAOTest {

    private EmpleadoDAO dao;

    @Before
    public void setUp() {
        // se ejecuta antes de cada test, inicializamos el DAO
        dao = new EmpleadoDAO();
    }

    // comprobamos que el admin existe en la base de datos
    @Test
    public void testBuscarPorEmailExistente() throws SQLException {
        Empleado emp = dao.buscarPorEmail("admin@tienda.com");
        assertNotNull("El empleado admin debe existir", emp);
        assertEquals("El cargo debe ser admin", "admin", emp.getCargo());
    }

    // si el email no existe debe devolver null, no lanzar una excepcion
    @Test
    public void testBuscarPorEmailInexistente() throws SQLException {
        Empleado emp = dao.buscarPorEmail("noexiste@tienda.com");
        assertNull("Un email que no existe debe devolver null", emp);
    }

    // el login con credenciales correctas debe devolver el empleado
    @Test
    public void testLoginCorrecto() throws SQLException {
        Empleado emp = dao.login("admin@tienda.com", "admin1234");
        assertNotNull("El login con credenciales correctas debe devolver el empleado", emp);
        assertEquals("El email debe coincidir", "admin@tienda.com", emp.getEmail());
    }

    // el login con contraseña incorrecta debe devolver null
    @Test
    public void testLoginContraseñaIncorrecta() throws SQLException {
        Empleado emp = dao.login("admin@tienda.com", "contraseñamalaaaa");
        assertNull("El login con contraseña incorrecta debe devolver null", emp);
    }

    // el login con un email que no existe debe devolver null
    @Test
    public void testLoginEmailInexistente() throws SQLException {
        Empleado emp = dao.login("noexiste@tienda.com", "12345");
        assertNull("El login con email inexistente debe devolver null", emp);
    }

    // listarTodos debe devolver al menos los empleados que insertamos en el SQL inicial
    @Test
    public void testListarTodosDevuelveResultados() throws SQLException {
        List<Empleado> lista = dao.listarTodos();
        assertNotNull("La lista no debe ser null", lista);
        assertTrue("Debe haber al menos un empleado", lista.size() > 0);
    }
}
