package primetechfinal.dao;

import java.sql.SQLException;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import primetechfinal.model.Cliente;

/**
 * Tests de integracion para ClienteDAO.
 * Comprueban que las operaciones con la base de datos funcionan correctamente.
 */
public class ClienteDAOTest {

    private ClienteDAO dao;

    @Before
    public void setUp() {
        dao = new ClienteDAO();
    }

    // debe devolver los clientes que insertamos en el SQL inicial
    @Test
    public void testListarTodosDevuelveResultados() throws SQLException {
        List<Cliente> lista = dao.listarTodos();
        assertNotNull("La lista no debe ser null", lista);
        assertTrue("Debe haber al menos un cliente", lista.size() > 0);
    }

    // debe haber al menos el cliente particular de ejemplo del SQL inicial
    @Test
    public void testListarParticulares() throws SQLException {
        List<Cliente> lista = dao.listarParticulares();
        assertNotNull("La lista no debe ser null", lista);
        assertTrue("Debe haber al menos un cliente particular", lista.size() > 0);
    }

    // debe haber al menos la empresa de ejemplo del SQL inicial
    @Test
    public void testListarEmpresas() throws SQLException {
        List<Cliente> lista = dao.listarEmpresas();
        assertNotNull("La lista no debe ser null", lista);
        assertTrue("Debe haber al menos una empresa", lista.size() > 0);
    }

    // insertar un cliente particular y comprobar que se puede recuperar
    @Test
    public void testInsertarYBuscarParticular() throws SQLException {
        Cliente c = new Cliente();
        c.setTipo("particular");
        c.setNombre("Test");
        c.setApellidos("JUnit");
        c.setDni("99999999T");
        c.setTelefono("600000000");
        c.setEmail("testjunit@test.com");
        c.setDireccion("Calle Test 1");

        dao.insertarParticular(c);

        // buscamos el cliente que acabamos de insertar
        List<Cliente> resultados = dao.buscarPorNombre("Test");
        assertFalse("Debe encontrar el cliente insertado", resultados.isEmpty());

        // limpiamos lo que hemos insertado para no ensuciar la BD
        dao.eliminar(resultados.get(0).getIdCliente());
    }

    // buscar por nombre inexistente debe devolver lista vacia
    @Test
    public void testBuscarPorNombreInexistente() throws SQLException {
        List<Cliente> lista = dao.buscarPorNombre("xyzClienteInexistente123");
        assertNotNull("La lista no debe ser null", lista);
        assertTrue("Debe devolver lista vacia si no hay coincidencias", lista.isEmpty());
    }
}
