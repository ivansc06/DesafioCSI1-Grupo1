package primetech.security;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import primetech.config.ConfiguracionApp;

/**
 * Controla los intentos de login para evitar ataques de fuerza bruta.
 * Bloquea una cuenta temporalmente tras superar el número máximo de intentos fallidos.
 * Singleton thread-safe.
 */
public class ControlAcceso {

    private static final ControlAcceso INSTANCIA = new ControlAcceso();

    private static final class RegistroIntento {
        int intentosFallidos = 0;
        LocalDateTime bloqueadoHasta = null;
    }

    private final Map<String, RegistroIntento> registros = new ConcurrentHashMap<>();

    private ControlAcceso() {}

    public static ControlAcceso getInstance() {
        return INSTANCIA;
    }

    /**
     * Comprueba si el email está actualmente bloqueado.
     * Si el bloqueo ha expirado, lo elimina automáticamente.
     */
    public boolean estaBloqueado(String email) {
        RegistroIntento r = registros.get(normalizar(email));
        if (r == null || r.bloqueadoHasta == null) return false;
        if (LocalDateTime.now().isAfter(r.bloqueadoHasta)) {
            r.bloqueadoHasta = null;
            r.intentosFallidos = 0;
            return false;
        }
        return true;
    }

    /**
     * Devuelve los segundos restantes de bloqueo, o 0 si no está bloqueado.
     */
    public long segundosRestantesBloqueo(String email) {
        RegistroIntento r = registros.get(normalizar(email));
        if (r == null || r.bloqueadoHasta == null) return 0;
        long segundos = Duration.between(LocalDateTime.now(), r.bloqueadoHasta).getSeconds();
        return Math.max(0, segundos);
    }

    /**
     * Registra un intento fallido. Si se supera el límite, bloquea la cuenta.
     */
    public void registrarIntentoFallido(String email) {
        RegistroIntento r = registros.computeIfAbsent(normalizar(email), k -> new RegistroIntento());
        r.intentosFallidos++;
        if (r.intentosFallidos >= ConfiguracionApp.getMaxIntentosFallidos()) {
            r.bloqueadoHasta = LocalDateTime.now()
                .plusMinutes(ConfiguracionApp.getTiempoBloqueoMinutos());
        }
    }

    /**
     * Limpia el registro al hacer login con éxito.
     */
    public void registrarLoginExitoso(String email) {
        registros.remove(normalizar(email));
    }

    /**
     * Devuelve cuántos intentos fallidos lleva la cuenta.
     */
    public int getIntentosFallidos(String email) {
        RegistroIntento r = registros.get(normalizar(email));
        return r == null ? 0 : r.intentosFallidos;
    }

    /**
     * Cuántos intentos le quedan antes del bloqueo.
     */
    public int intentosRestantes(String email) {
        int max = ConfiguracionApp.getMaxIntentosFallidos();
        return Math.max(0, max - getIntentosFallidos(email));
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
