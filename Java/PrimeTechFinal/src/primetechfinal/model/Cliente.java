package primetechfinal.model;

public class Cliente {

    private int    idCliente;
    private String tipo;       // "particular" | "empresa"
    private String telefono;
    private String email;
    private String direccion;

    // Particular
    private String nombre;
    private String apellidos;
    private String dni;

    // Empresa
    private String razonSocial;
    private String cif;
    private String contactoNombre;

    public Cliente() {}

   

    public String getNombreVisible() {
        if ("empresa".equals(getTipo())) {
            return getRazonSocial() != null ? getRazonSocial() : "Empresa sin nombre";
        }
        String n = getNombre() != null ? getNombre() : "";
        String a = getApellidos() != null ? getApellidos() : "";
        return (n + " " + a).trim();
    }

    @Override
    public String toString() {
        return "[" + (getTipo() != null ? getTipo().substring(0,1).toUpperCase() + getTipo().substring(1) : "?") + "] "
            + getNombreVisible();
    }

    /**
     * @return the idCliente
     */
    public int getIdCliente() {
        return idCliente;
    }

    /**
     * @param idCliente the idCliente to set
     */
    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    /**
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
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
     * @return the direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
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
     * @return the dni
     */
    public String getDni() {
        return dni;
    }

    /**
     * @param dni the dni to set
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * @return the razonSocial
     */
    public String getRazonSocial() {
        return razonSocial;
    }

    /**
     * @param razonSocial the razonSocial to set
     */
    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    /**
     * @return the cif
     */
    public String getCif() {
        return cif;
    }

    /**
     * @param cif the cif to set
     */
    public void setCif(String cif) {
        this.cif = cif;
    }

    /**
     * @return the contactoNombre
     */
    public String getContactoNombre() {
        return contactoNombre;
    }

    /**
     * @param contactoNombre the contactoNombre to set
     */
    public void setContactoNombre(String contactoNombre) {
        this.contactoNombre = contactoNombre;
    }
}
