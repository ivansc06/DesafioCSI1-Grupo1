package primetech.sesion;

import java.time.LocalDateTime;
import primetech.config.ConfiguracionApp;
import primetech.model.Empleado;

/**
 * Gestiona la sesión activa del empleado.
 * Incluye control de inactividad: si pasa más tiempo del configurado
 * sin actividad, la sesión se considera expirada.
 */
public class Sesion {

    private static Empleado empleadoActual = null;
    private static LocalDateTime ultimaActividad = null;

    private Sesion() {}

    public static void iniciar(Empleado emp) {
        empleadoActual = emp;
        ultimaActividad = LocalDateTime.now();
    }

    public static void cerrar() {
        empleadoActual   = null;
        ultimaActividad  = null;
    }

    /**
     * Actualiza el timestamp de última actividad para reiniciar el contador de inactividad.
     * Llamar desde cualquier acción del usuario en la UI.
     */
    public static void renovarActividad() {
        if (empleadoActual != null) {
            ultimaActividad = LocalDateTime.now();
        }
    }

    public static Empleado getEmpleado() {
        return empleadoActual;
    }

    public static boolean haySession() {
        return empleadoActual != null && !haExpirado();
    }

    public static boolean esAdmin() {
        return empleadoActual != null && "admin".equals(empleadoActual.getCargo());
    }

    /**
     * Devuelve true si el tiempo de inactividad supera el timeout configurado.
     * Si la sesión ha expirado, la cierra automáticamente.
     */
    public static boolean haExpirado() {
        if (ultimaActividad == null) return true;
        int timeoutMin = ConfiguracionApp.getTimeoutSesionMinutos();
        if (LocalDateTime.now().isAfter(ultimaActividad.plusMinutes(timeoutMin))) {
            cerrar();
            return true;
        }
        return false;
    }

    /**
     * Minutos restantes antes de que la sesión expire por inactividad.
     */
    public static long minutosHastaExpiracion() {
        if (ultimaActividad == null) return 0;
        int timeoutMin = ConfiguracionApp.getTimeoutSesionMinutos();
        LocalDateTime expira = ultimaActividad.plusMinutes(timeoutMin);
        long minutos = java.time.Duration.between(LocalDateTime.now(), expira).toMinutes();
        return Math.max(0, minutos);
    }
}
