package primetech.model;

public class Producto {

    private int    idProducto;
    private String nombre;
    private String descripcion;
    private double precioCompra;
    private double precioVenta;
    private int    stock;

    public Producto() {}

    public Producto(int idProducto, String nombre, String descripcion,
                    double precioCompra, double precioVenta, int stock) {
        this.idProducto   = idProducto;
        this.nombre       = nombre;
        this.descripcion  = descripcion;
        this.precioCompra = precioCompra;
        this.precioVenta  = precioVenta;
        this.stock        = stock;
    }

    public int    getIdProducto()   { return idProducto; }
    public String getNombre()       { return nombre; }
    public String getDescripcion()  { return descripcion; }
    public double getPrecioCompra() { return precioCompra; }
    public double getPrecioVenta()  { return precioVenta; }
    public int    getStock()        { return stock; }

    public void setIdProducto(int idProducto)       { this.idProducto = idProducto; }
    public void setNombre(String nombre)             { this.nombre = nombre; }
    public void setDescripcion(String descripcion)   { this.descripcion = descripcion; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }
    public void setPrecioVenta(double precioVenta)   { this.precioVenta = precioVenta; }
    public void setStock(int stock)                  { this.stock = stock; }

    @Override
    public String toString() { return nombre; }
}
