package primetech.model;

public class DetalleVenta {

    private int    idDetalle;
    private int    idVenta;
    private int    idProducto;
    private String nombreProducto;
    private int    cantidad;
    private double precioUnitario;

    public DetalleVenta() {}

    public DetalleVenta(int idProducto, String nombreProducto, int cantidad, double precioUnitario) {
        this.idProducto     = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad       = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int    getIdDetalle()      { return idDetalle; }
    public int    getIdVenta()        { return idVenta; }
    public int    getIdProducto()     { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public int    getCantidad()       { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getSubtotal()       { return cantidad * precioUnitario; }

    public void setIdDetalle(int idDetalle)         { this.idDetalle = idDetalle; }
    public void setIdVenta(int idVenta)             { this.idVenta = idVenta; }
    public void setIdProducto(int idProducto)       { this.idProducto = idProducto; }
    public void setNombreProducto(String n)         { this.nombreProducto = n; }
    public void setCantidad(int cantidad)           { this.cantidad = cantidad; }
    public void setPrecioUnitario(double p)         { this.precioUnitario = p; }
}
