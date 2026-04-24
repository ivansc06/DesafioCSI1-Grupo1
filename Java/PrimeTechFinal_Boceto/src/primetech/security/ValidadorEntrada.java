package primetech.security;

import primetech.exception.ValidacionException;

/**
 * Centraliza la validación de todos los datos de entrada antes de persistirlos.
 * Cada método lanza ValidacionException con un mensaje claro para mostrar al usuario.
 */
public class ValidadorEntrada {

    private static final int MAX_LONGITUD_TEXTO    = 255;
    private static final int MAX_LONGITUD_NOMBRE   = 100;
    private static final int MIN_LONGITUD_PASSWORD = 8;

    private static final String REGEX_EMAIL    = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
    private static final String REGEX_DNI      = "^[0-9]{8}[A-HJ-NP-TV-Z]$";
    private static final String REGEX_CIF      = "^[A-HJNPQRSUVW][0-9]{7}[0-9A-J]$";
    private static final String REGEX_TELEFONO = "^[+]?[0-9 ()\\-]{7,20}$";

    private ValidadorEntrada() {}

    // ── Campos de texto ──────────────────────────────────────────────────────

    public static void validarEmail(String email) throws ValidacionException {
        if (email == null || email.trim().isEmpty())
            throw new ValidacionException("El email no puede estar vacío.");
        if (email.length() > 150)
            throw new ValidacionException("El email es demasiado largo (máx. 150 caracteres).");
        if (!email.trim().matches(REGEX_EMAIL))
            throw new ValidacionException("El formato del email no es válido.");
    }

    public static void validarNombre(String valor, String campo) throws ValidacionException {
        if (valor == null || valor.trim().isEmpty())
            throw new ValidacionException("El campo '" + campo + "' no puede estar vacío.");
        if (valor.length() > MAX_LONGITUD_NOMBRE)
            throw new ValidacionException(
                "El campo '" + campo + "' es demasiado largo (máx. " + MAX_LONGITUD_NOMBRE + " caracteres).");
    }

    public static void validarTexto(String valor, String campo) throws ValidacionException {
        if (valor != null && valor.length() > MAX_LONGITUD_TEXTO)
            throw new ValidacionException(
                "El campo '" + campo + "' es demasiado largo (máx. " + MAX_LONGITUD_TEXTO + " caracteres).");
    }

    // ── Documentos de identidad ──────────────────────────────────────────────

    public static void validarDni(String dni) throws ValidacionException {
        if (dni == null || dni.trim().isEmpty())
            throw new ValidacionException("El DNI no puede estar vacío.");
        String dniNorm = dni.trim().toUpperCase();
        if (!dniNorm.matches(REGEX_DNI))
            throw new ValidacionException("El DNI debe tener 8 dígitos seguidos de una letra válida.");
        if (!validarLetraDni(dniNorm))
            throw new ValidacionException("La letra del DNI no es correcta para ese número.");
    }

    public static void validarCif(String cif) throws ValidacionException {
        if (cif == null || cif.trim().isEmpty())
            throw new ValidacionException("El CIF no puede estar vacío.");
        if (!cif.trim().toUpperCase().matches(REGEX_CIF))
            throw new ValidacionException("El formato del CIF no es válido.");
    }

    public static void validarTelefono(String telefono) throws ValidacionException {
        if (telefono == null || telefono.trim().isEmpty()) return;
        if (!telefono.trim().matches(REGEX_TELEFONO))
            throw new ValidacionException("El formato del teléfono no es válido.");
    }

    // ── Contraseña ───────────────────────────────────────────────────────────

    /**
     * Exige mínimo 8 caracteres, una mayúscula, una minúscula y un dígito.
     */
    public static void validarContraseña(String contraseña) throws ValidacionException {
        if (contraseña == null || contraseña.length() < MIN_LONGITUD_PASSWORD)
            throw new ValidacionException(
                "La contraseña debe tener al menos " + MIN_LONGITUD_PASSWORD + " caracteres.");
        if (!contraseña.matches(".*[A-Z].*"))
            throw new ValidacionException("La contraseña debe contener al menos una letra mayúscula.");
        if (!contraseña.matches(".*[a-z].*"))
            throw new ValidacionException("La contraseña debe contener al menos una letra minúscula.");
        if (!contraseña.matches(".*[0-9].*"))
            throw new ValidacionException("La contraseña debe contener al menos un número.");
    }

    // ── Valores numéricos ────────────────────────────────────────────────────

    public static void validarPrecio(double precio, String campo) throws ValidacionException {
        if (precio < 0)
            throw new ValidacionException("El campo '" + campo + "' no puede ser negativo.");
        if (precio > 1_000_000)
            throw new ValidacionException("El campo '" + campo + "' supera el valor máximo permitido (1.000.000).");
    }

    public static void validarStock(int stock) throws ValidacionException {
        if (stock < 0)
            throw new ValidacionException("El stock no puede ser negativo.");
        if (stock > 999_999)
            throw new ValidacionException("El stock supera el valor máximo permitido (999.999).");
    }

    public static void validarCantidad(int cantidad) throws ValidacionException {
        if (cantidad <= 0)
            throw new ValidacionException("La cantidad debe ser mayor que cero.");
        if (cantidad > 9_999)
            throw new ValidacionException("La cantidad no puede superar 9.999 unidades.");
    }

    // ── Utilidades LIKE ──────────────────────────────────────────────────────

    /**
     * Escapa los metacaracteres de LIKE en MySQL (%, _, \) para evitar
     * que las búsquedas devuelvan resultados inesperados.
     */
    public static String escaparLike(String texto) {
        if (texto == null) return "";
        return texto
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }

    // ── Privado ──────────────────────────────────────────────────────────────

    private static boolean validarLetraDni(String dniNorm) {
        final String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        try {
            int numero = Integer.parseInt(dniNorm.substring(0, 8));
            return dniNorm.charAt(8) == letras.charAt(numero % 23);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
