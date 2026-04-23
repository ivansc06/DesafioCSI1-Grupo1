package primetech.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.*;

/**
 * Logger centralizado que escribe en consola y en archivos rotativos
 * en ~/PrimeTechFinal/logs/. Máximo 5 archivos de 5 MB cada uno.
 */
public class AppLogger {

    private static final String DIR_LOGS =
        System.getProperty("user.home") + "/PrimeTechFinal/logs/";

    private static final Logger logger = Logger.getLogger("primetech");

    static {
        try {
            Files.createDirectories(Paths.get(DIR_LOGS));
            FileHandler fh = new FileHandler(
                DIR_LOGS + "primetech_%g.log",
                5 * 1024 * 1024,
                5,
                true
            );
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.ALL);
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);

            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.WARNING);
            ch.setFormatter(new SimpleFormatter());
            logger.addHandler(ch);

            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            Logger.getLogger(AppLogger.class.getName())
                  .log(Level.WARNING, "No se pudo configurar el log en archivo", e);
        }
    }

    private AppLogger() {}

    public static void info(String mensaje) {
        logger.info(mensaje);
    }

    public static void warning(String mensaje) {
        logger.warning(mensaje);
    }

    public static void error(String mensaje, Throwable t) {
        logger.log(Level.SEVERE, mensaje, t);
    }

    public static void error(String mensaje) {
        logger.severe(mensaje);
    }

    /**
     * Registra una acción de auditoría (quién hizo qué y cuándo).
     * Estos mensajes se graban siempre, independientemente del nivel configurado.
     */
    public static void auditoria(String mensaje) {
        logger.log(Level.INFO, "[AUDITORIA] " + mensaje);
    }

    public static void debug(String mensaje) {
        logger.fine(mensaje);
    }
}
