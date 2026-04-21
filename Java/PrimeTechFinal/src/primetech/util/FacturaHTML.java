package primetech.util;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import primetech.model.DetalleVenta;
import primetech.model.Venta;

public class FacturaHTML {

    // codigo para abrir archivo
    //ProcessBuilder pb = new ProcessBuilder("nombre.exe");
    //Process proceso = pb.start();//abrimos
    //proceso.destroy();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static Path generarYAbrir(Venta venta, String empresa) throws IOException {
        String html = generarHTML(venta, empresa);

        Path dir = Paths.get(System.getProperty("user.home"), "PrimeTechFinal", "facturas");
        Files.createDirectories(dir);

        Path archivo = dir.resolve("factura_" + venta.getIdVenta() + ".html");
        Files.write(archivo, html.getBytes(StandardCharsets.UTF_8));

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(archivo.toUri());
        }
        return archivo;
    }

    private static String generarHTML(Venta venta, String empresa) {
        StringBuilder sb = new StringBuilder();
        double subtotalSinIva = venta.getTotal() / 1.21;
        double iva = venta.getTotal() - subtotalSinIva;
        String fecha   = venta.getFechaVenta() != null ? venta.getFechaVenta().format(FMT) : "-";
        String cliente = venta.getNombreCliente() != null ? venta.getNombreCliente() : "Sin cliente registrado";
        String metodo  = capitalize(venta.getMetodoPago() != null ? venta.getMetodoPago() : "efectivo");

        sb.append("<!DOCTYPE html>\n<html lang='es'>\n<head>\n")
          .append("<meta charset='UTF-8'>\n")
          .append("<title>Factura #").append(venta.getIdVenta()).append("</title>\n")
          .append("<style>\n")
          .append("  * { box-sizing: border-box; margin: 0; padding: 0; }\n")
          .append("  body { font-family: 'Segoe UI', Arial, sans-serif; color: #333; background: #f5f5f5; }\n")
          .append("  .factura { max-width: 820px; margin: 30px auto; background: #fff; padding: 50px; border-radius: 8px; box-shadow: 0 2px 20px rgba(0,0,0,0.1); }\n")
          .append("  .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 40px; }\n")
          .append("  .logo h1 { font-size: 28px; color: #2c3e50; font-weight: 700; }\n")
          .append("  .logo p { color: #7f8c8d; font-size: 13px; margin-top: 4px; }\n")
          .append("  .factura-info { text-align: right; }\n")
          .append("  .factura-info h2 { font-size: 32px; color: #3498db; font-weight: 300; }\n")
          .append("  .factura-info p { color: #555; font-size: 13px; margin-top: 4px; }\n")
          .append("  .divider { border: none; border-top: 2px solid #ecf0f1; margin: 25px 0; }\n")
          .append("  .datos { display: flex; justify-content: space-between; margin-bottom: 35px; }\n")
          .append("  .datos-bloque h3 { font-size: 11px; color: #95a5a6; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }\n")
          .append("  .datos-bloque p { font-size: 14px; color: #2c3e50; line-height: 1.6; }\n")
          .append("  table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }\n")
          .append("  thead tr { background: #2c3e50; color: white; }\n")
          .append("  thead th { padding: 12px 15px; text-align: left; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }\n")
          .append("  thead th:last-child, thead th:nth-child(3), thead th:nth-child(2) { text-align: right; }\n")
          .append("  tbody tr:nth-child(even) { background: #f8f9fa; }\n")
          .append("  tbody td { padding: 12px 15px; font-size: 14px; border-bottom: 1px solid #ecf0f1; }\n")
          .append("  tbody td:nth-child(2), tbody td:nth-child(3), tbody td:last-child { text-align: right; }\n")
          .append("  .totales { margin-left: auto; width: 280px; }\n")
          .append("  .totales table { margin-bottom: 0; }\n")
          .append("  .totales td { padding: 8px 15px; font-size: 14px; border: none; }\n")
          .append("  .totales td:last-child { text-align: right; }\n")
          .append("  .total-final { background: #2c3e50 !important; color: white; font-weight: bold; font-size: 16px !important; }\n")
          .append("  .footer { margin-top: 50px; text-align: center; color: #95a5a6; font-size: 12px; }\n")
          .append("  .badge-pago { display: inline-block; background: #27ae60; color: white; padding: 3px 10px; border-radius: 12px; font-size: 12px; }\n")
          .append("  @media print { body { background: white; } .factura { box-shadow: none; margin: 0; padding: 30px; } }\n")
          .append("</style>\n</head>\n<body>\n")
          .append("<div class='factura'>\n")
          .append("  <div class='header'>\n")
          .append("    <div class='logo'><h1>").append(escape(empresa)).append("</h1>")
          .append("<p>Tienda de informática y tecnología</p></div>\n")
          .append("    <div class='factura-info'><h2>FACTURA</h2>")
          .append("<p><strong>#").append(venta.getIdVenta()).append("</strong></p>")
          .append("<p>Fecha: ").append(fecha).append("</p>")
          .append("<p>Pago: <span class='badge-pago'>").append(metodo).append("</span></p>")
          .append("</div>\n  </div>\n")
          .append("  <hr class='divider'>\n")
          .append("  <div class='datos'>\n")
          .append("    <div class='datos-bloque'><h3>Emisor</h3><p>")
          .append(escape(empresa)).append("<br>CIF: A12345678<br>Zaragoza, España<br>info@primetechfinal.es</p></div>\n")
          .append("    <div class='datos-bloque'><h3>Cliente</h3><p>").append(escape(cliente)).append("</p></div>\n")
          .append("  </div>\n")
          .append("  <table>\n<thead><tr>")
          .append("<th>Descripción</th><th>Precio unit.</th><th>Cant.</th><th>Subtotal</th>")
          .append("</tr></thead>\n<tbody>\n");

        for (DetalleVenta d : venta.getDetalles()) {
            sb.append("    <tr>")
              .append("<td>").append(escape(d.getNombreProducto())).append("</td>")
              .append("<td>").append(formatEuro(d.getPrecioUnitario())).append("</td>")
              .append("<td>").append(d.getCantidad()).append("</td>")
              .append("<td>").append(formatEuro(d.getSubtotal())).append("</td>")
              .append("</tr>\n");
        }

        sb.append("  </tbody>\n</table>\n")
          .append("  <div class='totales'><table>\n")
          .append("    <tr><td>Base imponible</td><td>").append(formatEuro(subtotalSinIva)).append("</td></tr>\n")
          .append("    <tr><td>IVA (21%)</td><td>").append(formatEuro(iva)).append("</td></tr>\n")
          .append("    <tr class='total-final'><td>TOTAL</td><td>").append(formatEuro(venta.getTotal())).append("</td></tr>\n")
          .append("  </table></div>\n")
          .append("  <div class='footer'>\n")
          .append("    <p>Gracias por su compra &mdash; ").append(escape(empresa)).append("</p>\n")
          .append("    <p style='margin-top:6px'>Para dudas contacte: info@primetechfinal.es | Tel: 976 000 000</p>\n")
          .append("  </div>\n")
          .append("</div>\n</body>\n</html>");

        return sb.toString();
    }

    private static String formatEuro(double value) {
        return String.format(Locale.GERMANY, "%.2f €", value);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
