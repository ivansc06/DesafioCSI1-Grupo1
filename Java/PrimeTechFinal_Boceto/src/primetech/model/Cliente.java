package primetech.model;

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

    public int    getIdCliente() { return idCliente; }
    public String getTipo()      { return tipo; }
    public String getTelefono()  { return telefono; }
    public String getEmail()     { return email; }
    public String getDireccion() { return direccion; }

    public void setIdCliente(int idCliente)    { this.idCliente = idCliente; }
    public void setTipo(String tipo)            { this.tipo = tipo; }
    public void setTelefono(String telefono)    { this.telefono = telefono; }
    public void setEmail(String email)          { this.email = email; }
    public void setDireccion(String direccion)  { this.direccion = direccion; }

    public String getNombre()    { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getDni()       { return dni; }
    public void setNombre(String nombre)        { this.nombre = nombre; }
    public void setApellidos(String apellidos)  { this.apellidos = apellidos; }
    public void setDni(String dni)              { this.dni = dni; }

    public String getRazonSocial()    { return razonSocial; }
    public String getCif()            { return cif; }
    public String getContactoNombre() { return contactoNombre; }
    public void setRazonSocial(String razonSocial)       { this.razonSocial = razonSocial; }
    public void setCif(String cif)                        { this.cif = cif; }
    public void setContactoNombre(String contactoNombre) { this.contactoNombre = contactoNombre; }

    public String getNombreVisible() {
        if ("empresa".equals(tipo)) {
            return razonSocial != null ? razonSocial : "Empresa sin nombre";
        }
        String n = nombre != null ? nombre : "";
        String a = apellidos != null ? apellidos : "";
        return (n + " " + a).trim();
    }

    @Override
    public String toString() {
        return "[" + (tipo != null ? tipo.substring(0,1).toUpperCase() + tipo.substring(1) : "?") + "] "
            + getNombreVisible();
    }
}
