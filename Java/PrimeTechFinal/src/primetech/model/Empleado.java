package primetech.model;

public class Empleado {

    private int    idEmpleado;
    private String nombre;
    private String apellidos;
    private String cargo;
    private String email;
    private String contraseña;

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

    public int    getIdEmpleado() { return idEmpleado; }
    public String getNombre()     { return nombre; }
    public String getApellidos()  { return apellidos; }
    public String getCargo()      { return cargo; }
    public String getEmail()      { return email; }
    public String getContraseña() { return contraseña; }

    public void setIdEmpleado(int idEmpleado)    { this.idEmpleado = idEmpleado; }
    public void setNombre(String nombre)          { this.nombre = nombre; }
    public void setApellidos(String apellidos)    { this.apellidos = apellidos; }
    public void setCargo(String cargo)            { this.cargo = cargo; }
    public void setEmail(String email)            { this.email = email; }
    public void setContraseña(String contraseña)  { this.contraseña = contraseña; }

    public String getNombreCompleto() {
        return nombre + " " + (apellidos != null ? apellidos : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + cargo + ")";
    }
}
