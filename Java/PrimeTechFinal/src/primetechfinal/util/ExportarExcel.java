package primetechfinal.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import primetechfinal.dao.ClienteDAO;
import primetechfinal.dao.ProductoDAO;
import primetechfinal.dao.VentaDAO;
import primetechfinal.model.Cliente;
import primetechfinal.model.Producto;
import primetechfinal.model.Venta;

public class ExportarExcel {

    // formateador de fechas para la columna fecha de las ventas
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // metodo principal que genera el excel con las tres pestañas
    public static void exportarTodo(String rutaArchivo) throws SQLException, IOException {

        try (XSSFWorkbook libro = new XSSFWorkbook()) {

            // estilo para las cabeceras de cada tabla (negrita)
            CellStyle estiloCabecera = libro.createCellStyle();
            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            estiloCabecera.setFont(fuenteNegrita);

            // creamos las tres pestañas
            crearHojaProductos(libro, estiloCabecera);
            crearHojaVentas(libro, estiloCabecera);
            crearHojaClientes(libro, estiloCabecera);

            // guardamos el archivo en la ruta que nos pasan
            try (FileOutputStream fos = new FileOutputStream(rutaArchivo)) {
                libro.write(fos);
            }
        }
    }

    private static void crearHojaProductos(XSSFWorkbook libro, CellStyle estiloCabecera) throws SQLException {
        Sheet hoja = libro.createSheet("Productos");

        // cabecera
        Row cabecera = hoja.createRow(0);
        String[] columnas = {"ID", "Nombre", "Descripcion", "Precio Compra", "Precio Venta", "Stock"};
        for (int i = 0; i < columnas.length; i++) {
            Cell celda = cabecera.createCell(i);
            celda.setCellValue(columnas[i]);
            celda.setCellStyle(estiloCabecera);
        }

        // datos
        List<Producto> productos = new ProductoDAO().listarTodos();
        int numFila = 1;
        for (Producto p : productos) {
            Row fila = hoja.createRow(numFila++);
            fila.createCell(0).setCellValue(p.getIdProducto());
            fila.createCell(1).setCellValue(p.getNombre());
            fila.createCell(2).setCellValue(p.getDescripcion() != null ? p.getDescripcion() : "");
            fila.createCell(3).setCellValue(p.getPrecioCompra());
            fila.createCell(4).setCellValue(p.getPrecioVenta());
            fila.createCell(5).setCellValue(p.getStock());
        }

        // ajustamos el ancho de cada columna al contenido
        for (int i = 0; i < columnas.length; i++) {
            hoja.autoSizeColumn(i);
        }
    }

    private static void crearHojaVentas(XSSFWorkbook libro, CellStyle estiloCabecera) throws SQLException {
        Sheet hoja = libro.createSheet("Ventas");

        // cabecera
        Row cabecera = hoja.createRow(0);
        String[] columnas = {"ID Venta", "Cliente", "Fecha", "Metodo Pago", "Total"};
        for (int i = 0; i < columnas.length; i++) {
            Cell celda = cabecera.createCell(i);
            celda.setCellValue(columnas[i]);
            celda.setCellStyle(estiloCabecera);
        }

        // datos
        List<Venta> ventas = new VentaDAO().listarTodas();
        int numFila = 1;
        for (Venta v : ventas) {
            Row fila = hoja.createRow(numFila++);
            fila.createCell(0).setCellValue(v.getIdVenta());
            fila.createCell(1).setCellValue(v.getNombreCliente() != null ? v.getNombreCliente() : "Sin cliente");
            fila.createCell(2).setCellValue(v.getFechaVenta() != null ? v.getFechaVenta().format(FORMATO_FECHA) : "");
            fila.createCell(3).setCellValue(v.getMetodoPago());
            fila.createCell(4).setCellValue(v.getTotal());
        }

        for (int i = 0; i < columnas.length; i++) {
            hoja.autoSizeColumn(i);
        }
    }

    private static void crearHojaClientes(XSSFWorkbook libro, CellStyle estiloCabecera) throws SQLException {
        Sheet hoja = libro.createSheet("Clientes");

        // cabecera
        Row cabecera = hoja.createRow(0);
        String[] columnas = {"ID", "Tipo", "Nombre / Razon Social", "Telefono", "Email", "Direccion"};
        for (int i = 0; i < columnas.length; i++) {
            Cell celda = cabecera.createCell(i);
            celda.setCellValue(columnas[i]);
            celda.setCellStyle(estiloCabecera);
        }

        // datos
        List<Cliente> clientes = new ClienteDAO().listarTodos();
        int numFila = 1;
        for (Cliente c : clientes) {
            Row fila = hoja.createRow(numFila++);
            fila.createCell(0).setCellValue(c.getIdCliente());
            fila.createCell(1).setCellValue(c.getTipo());
            fila.createCell(2).setCellValue(c.getNombreVisible());
            fila.createCell(3).setCellValue(c.getTelefono() != null ? c.getTelefono() : "");
            fila.createCell(4).setCellValue(c.getEmail() != null ? c.getEmail() : "");
            fila.createCell(5).setCellValue(c.getDireccion() != null ? c.getDireccion() : "");
        }

        for (int i = 0; i < columnas.length; i++) {
            hoja.autoSizeColumn(i);
        }
    }
}
