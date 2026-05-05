package primetechfinal.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import primetechfinal.model.DetalleVenta;
import primetechfinal.model.Venta;

// genera un codigo QR con el resumen de la factura
// el QR se devuelve como imagen en Base64 para incrustarlo directamente en el HTML
public class GenerarQR {

    // genera el QR de la factura y lo devuelve como cadena Base64
    // asi podemos meterlo en el HTML sin necesidad de guardar un archivo aparte
    public static String generarQRBase64(Venta venta, String empresa) throws WriterException, IOException {
        String contenido = construirTextoQR(venta, empresa);

        // configuramos el QR para que use UTF-8 y tenga margen minimo
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        // generamos la matriz de bits del QR de 200x200 pixeles
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 200, 200, hints);
        BufferedImage imagen = MatrixToImageWriter.toBufferedImage(matrix);

        // convertimos la imagen a Base64 para poder meterla en el HTML como src
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imagen, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // construye el texto que va dentro del QR con el resumen de la factura
    private static String construirTextoQR(Venta venta, String empresa) {
        StringBuilder sb = new StringBuilder();
        sb.append(empresa).append("\n");
        sb.append("Factura #").append(venta.getIdVenta()).append("\n");

        if (venta.getFechaVenta() != null) {
            sb.append("Fecha: ").append(
                venta.getFechaVenta().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).append("\n");
        }

        if (venta.getNombreCliente() != null) {
            sb.append("Cliente: ").append(venta.getNombreCliente()).append("\n");
        }

        sb.append("---\n");

        // añadimos cada producto con su cantidad y precio
        for (DetalleVenta d : venta.getDetalles()) {
            sb.append(d.getNombreProducto())
              .append(" x").append(d.getCantidad())
              .append(" -> ").append(String.format("%.2f", d.getSubtotal())).append("€\n");
        }

        sb.append("---\n");
        sb.append("TOTAL: ").append(String.format("%.2f", venta.getTotal())).append("€\n");
        sb.append("Pago: ").append(venta.getMetodoPago());

        return sb.toString();
    }
}
