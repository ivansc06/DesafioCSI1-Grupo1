package primetechfinal.dao;

import java.sql.SQLException;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import primetechfinal.model.Producto;

/**
 * Tests de integracion para ProductoDAO.
 * Comprueban que las operaciones con la base de datos funcionan correctamente.
 */
public class ProductoDAOTest {

    private ProductoDAO dao;

    @Before
    public void setUp() {
        dao = new ProductoDAO();
    }

    // debe devolver los productos que insertamos en el SQL inicial
    @Test
    public void testListarTodosDevuelveResultados() throws SQLException {
        List<Producto> lista = dao.listarTodos();
        assertNotNull("La lista no debe ser null", lista);
        assertTrue("Debe haber al menos un producto", lista.size() > 0);
    }

    // insertar un producto y comprobar que se puede recuperar despues
    @Test
    public void testInsertarYBuscar() throws SQLException {
        Producto p = new Producto();
        p.setNombre("Producto Test JUnit");
        p.setDescripcion("Descripcion de prueba");
        p.setPrecioCompra(10.00);
        p.setPrecioVenta(19.99);
        p.setStock(5);

        dao.insertar(p);

        // buscamos el producto que acabamos de insertar
        List<Producto> resultados = dao.buscarPorNombre("Producto Test JUnit");
        assertFalse("Debe encontrar el producto insertado", resultados.isEmpty());
        assertEquals("El nombre debe coincidir", "Producto Test JUnit", resultados.get(0).getNombre());

        // limpiamos lo que hemos insertado para no ensuciar la BD
        dao.eliminar(resultados.get(0).getIdProducto());
    }

    // buscar por nombre con un texto que no existe debe devolver lista vacia
    @Test
    public void testBuscarPorNombreInexistente() throws SQLException {
        List<Producto> lista = dao.buscarPorNombre("xyzproductoinexistente123");
        assertNotNull("La lista no debe ser null", lista);
        assertTrue("Debe devolver lista vacia si no hay coincidencias", lista.isEmpty());
    }

    // buscarPorId con un id que no existe debe devolver null
    @Test
    public void testBuscarPorIdInexistente() throws SQLException {
        Producto p = dao.buscarPorId(999999);
        assertNull("Un id que no existe debe devolver null", p);
    }
}
