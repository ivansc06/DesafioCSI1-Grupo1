package primetechfinal.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import primetechfinal.model.DetalleVenta;
import primetechfinal.model.Producto;
import primetechfinal.model.Venta;

/**
 * Tests de integracion para VentaDAO.
 * Comprueban que las operaciones con la base de datos funcionan correctamente.
 */
public class VentaDAOTest {

    private VentaDAO ventaDAO;
    private ProductoDAO productoDAO;

    @Before
    public void setUp() {
        ventaDAO = new VentaDAO();
        productoDAO = new ProductoDAO();
    }

    // listarTodas no debe lanzar excepcion ni devolver null
    @Test
    public void testListarTodasNoDevuelveNull() throws SQLException {
        assertNotNull("La lista de ventas no debe ser null", ventaDAO.listarTodas());
    }

    // registrar una venta y comprobar que aparece en el listado
    @Test
    public void testRegistrarYEliminarVenta() throws SQLException {
        // cogemos el primer producto disponible para usarlo en la venta
        List<Producto> productos = productoDAO.listarTodos();
        assertFalse("Debe haber productos para poder hacer el test", productos.isEmpty());

        Producto p = productos.get(0);
        int stockAntes = p.getStock();

        // solo hacemos el test si hay stock suficiente
        if (stockAntes < 1) return;

        // creamos una venta de prueba con un detalle
        Venta venta = new Venta();
        venta.setIdEmpleado(1); // usamos el admin
        venta.setMetodoPago("efectivo");

        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdProducto(p.getIdProducto());
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(p.getPrecioVenta());

        List<DetalleVenta> detalles = new ArrayList<>();
        detalles.add(detalle);
        venta.setDetalles(detalles);

        // registramos la venta
        int idVenta = ventaDAO.registrarVenta(venta);
        assertTrue("El id de la venta generada debe ser mayor que 0", idVenta > 0);

        // comprobamos que el stock bajo correctamente
        Producto pDespues = productoDAO.buscarPorId(p.getIdProducto());
        assertEquals("El stock debe haber bajado en 1", stockAntes - 1, pDespues.getStock());

        // eliminamos la venta y comprobamos que el stock se restaura
        ventaDAO.eliminar(idVenta);
        Producto pRestaurado = productoDAO.buscarPorId(p.getIdProducto());
        assertEquals("El stock debe haberse restaurado al eliminar la venta", stockAntes, pRestaurado.getStock());
    }

    // cargar detalles de una venta inexistente debe devolver lista vacia
    @Test
    public void testCargarDetallesVentaInexistente() throws SQLException {
        List<DetalleVenta> detalles = ventaDAO.cargarDetalles(999999);
        assertNotNull("Los detalles no deben ser null", detalles);
        assertTrue("Una venta inexistente no debe tener detalles", detalles.isEmpty());
    }

    // el total de ventas de hoy debe ser un numero mayor o igual a 0
    @Test
    public void testTotalVentasHoy() throws SQLException {
        double total = ventaDAO.totalVentasHoy();
        assertTrue("El total de ventas de hoy debe ser >= 0", total >= 0);
    }

    // el numero de ventas del mes debe ser mayor o igual a 0
    @Test
    public void testVentasMes() throws SQLException {
        int ventas = ventaDAO.ventasMes();
        assertTrue("El numero de ventas del mes debe ser >= 0", ventas >= 0);
    }
}
