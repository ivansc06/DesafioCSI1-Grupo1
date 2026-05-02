package primetechfinal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import primetechfinal.model.Venta;

/**
 * Clase para enviar facturas por email al cliente.
 * La configuracion del servidor de correo esta en config.properties.
 */
public class EnviarEmail {

    // cargamos la configuracion del correo desde config.properties
    private static final Properties config = cargarConfig();

    private static Properties cargarConfig() {
        Properties props = new Properties();
        try (InputStream in = EnviarEmail.class.getResourceAsStream("/primetechfinal/email.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar config.properties", e);
        }
        return props;
    }

    /**
     * Envia la factura de una venta por email al cliente.
     * El email del cliente debe estar informado en la venta.
     *
     * @param venta   la venta con sus detalles y datos del cliente
     * @param empresa nombre de la empresa que aparece en la factura
     * @throws Exception si hay algun problema al enviar el email
     */
    public static void enviarFactura(Venta venta, String empresa) throws Exception {

        String emailDestino = venta.getEmailCliente();
        if (emailDestino == null || emailDestino.trim().isEmpty()) {
            throw new IllegalArgumentException("El cliente no tiene email registrado.");
        }

        // generamos el HTML de la factura para usarlo como cuerpo del email
        String htmlFactura = FacturaHTML.generarHTMLParaEmail(venta, empresa);

        // configuramos la conexion con el servidor SMTP
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.host", config.getProperty("email.host"));
        smtpProps.put("mail.smtp.port", config.getProperty("email.port"));
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");

        final String usuario = config.getProperty("email.usuario");
        final String clave   = config.getProperty("email.clave");

        // creamos la sesion autenticada con el servidor de correo
        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, clave);
            }
        });

        // construimos el mensaje
        Message mensaje = new MimeMessage(session);
        mensaje.setFrom(new InternetAddress(usuario, config.getProperty("email.nombre")));
        mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
        mensaje.setSubject("Factura #" + venta.getIdVenta() + " - " + empresa);

        // el cuerpo del email es el HTML de la factura
        mensaje.setContent(htmlFactura, "text/html; charset=UTF-8");

        // enviamos
        Transport.send(mensaje);
    }
}
