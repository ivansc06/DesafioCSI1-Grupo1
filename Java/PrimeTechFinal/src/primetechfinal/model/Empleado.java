package primetechfinal.model;

public class Empleado {

    private int    idEmpleado;
    private String nombre;
    private String apellidos;
    private String cargo;
    private String email;
    private String contraseña;
    // campos para controlar los intentos fallidos y el bloqueo de la cuenta
    private int     intentosFallidos;
    private boolean bloqueado;

    public Empleado() {}

    public Empleado(int idEmpleado, String nombre, String apellidos,
                    String cargo, String email, String contraseña) {
        this.idEmpleado = idEmpleado;
        this.nombre     = nombre;
        this.apellidos  = apellidos;
        this.cargo      = cargo;
        this.email      = email;
        this.contraseña = contraseña;
    }

    

    public String getNombreCompleto() {
        return getNombre() + " " + (getApellidos() != null ? getApellidos() : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + getCargo() + ")";
    }

    /**
     * @return the idEmpleado
     */
    public int getIdEmpleado() {
        return idEmpleado;
    }

    /**
     * @param idEmpleado the idEmpleado to set
     */
    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the apellidos
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * @param apellidos the apellidos to set
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * @return the cargo
     */
    public String getCargo() {
        return cargo;
    }

    /**
     * @param cargo the cargo to set
     */
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the contraseña
     */
    public String getContraseña() {
        return contraseña;
    }

    /**
     * @param contraseña the contraseña to set
     */
    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    /**
     * @return the intentosFallidos
     */
    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    /**
     * @param intentosFallidos the intentosFallidos to set
     */
    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    /**
     * @return the bloqueado
     */
    public boolean isBloqueado() {
        return bloqueado;
    }

    /**
     * @param bloqueado the bloqueado to set
     */
    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }
}
