package primetech.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {

    private int           idVenta;
    private int           idCliente;
    private int           idEmpleado;
    private LocalDateTime fechaVenta;
    private double        total;
    private String        metodoPago;   // efectivo | tarjeta | transferencia

    private String        nombreCliente;
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {}

    public int           getIdVenta()       { return idVenta; }
    public int           getIdCliente()     { return idCliente; }
    public int           getIdEmpleado()    { return idEmpleado; }
    public LocalDateTime getFechaVenta()    { return fechaVenta; }
    public double        getTotal()         { return total; }
    public String        getMetodoPago()    { return metodoPago; }
    public String        getNombreCliente() { return nombreCliente; }
    public List<DetalleVenta> getDetalles() { return detalles; }

    public void setIdVenta(int idVenta)              { this.idVenta = idVenta; }
    public void setIdCliente(int idCliente)          { this.idCliente = idCliente; }
    public void setIdEmpleado(int idEmpleado)        { this.idEmpleado = idEmpleado; }
    public void setFechaVenta(LocalDateTime f)       { this.fechaVenta = f; }
    public void setTotal(double total)               { this.total = total; }
    public void setMetodoPago(String metodoPago)     { this.metodoPago = metodoPago; }
    public void setNombreCliente(String nc)          { this.nombreCliente = nc; }
    public void setDetalles(List<DetalleVenta> d)    { this.detalles = d; }
}
