package primetech.exception;

public class PrimeTechException extends Exception {

    public PrimeTechException(String mensaje) {
        super(mensaje);
    }

    public PrimeTechException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
