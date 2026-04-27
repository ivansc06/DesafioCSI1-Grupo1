package primetechfinal.sesion;

import primetechfinal.model.Empleado;

public class Sesion {

    private static Empleado empleadoActual = null;

    private Sesion() {}

    public static void iniciar(Empleado emp) {
        empleadoActual = emp;
    }

    public static void cerrar() {
        empleadoActual = null;
    }

    public static Empleado getEmpleado() {
        return empleadoActual;
    }

    public static boolean haySession() {
        return empleadoActual != null;
    }

    public static boolean esAdmin() {
        return empleadoActual != null && "admin".equals(empleadoActual.getCargo());
    }
    
}
