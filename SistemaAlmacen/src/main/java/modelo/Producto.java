package modelo;

import java.util.Date;
import java.util.Objects;
public abstract class Producto {

    private String nombreProducto;
    private double precioProducto;
    private Date fechaVencimiento;
    private int stock;
    private String marcaProducto;
    private long id;
    private static long ultimoId = 1; //


    public Producto(String nombreProducto, double precioProducto, Date fechaVencimiento,
                    int stock, String marcaProducto){
        this.nombreProducto = nombreProducto;
        this.precioProducto = precioProducto;
        this.fechaVencimiento = fechaVencimiento;
        this.stock = stock;
        this.marcaProducto = marcaProducto;
        this.ultimoId = ultimoId++;
        this.id=ultimoId;

    }

    //getters y setters


    public long getId() {
        return id;
    }

    public static long getUltimoId() {
        return ultimoId;
    }

    public static void setUltimoId(long nuevoUltimoId) {
        ultimoId = nuevoUltimoId;
    }

    public String getNombreProducto(){
        return nombreProducto;
    }

    public double getPrecioProducto(){
        return precioProducto;
    }

    public int getStock(){
        return stock;
    }

    public Date getFechaVencimiento(){
        return fechaVencimiento;
    }

    public String getMarcaProducto(){
        return marcaProducto;
    }

    public void setMarcaProducto(String nuevaMarcaProducto){
        this.marcaProducto = nuevaMarcaProducto;
    }

    public void setNombreProducto(String nuevoNombreProducto){
        this.nombreProducto = nuevoNombreProducto;
    }

    public void setPrecioProducto(int nuevoPrecioProducto){
        this.precioProducto = nuevoPrecioProducto;
    }

    public void setStock(int nuevoStock){
        this.stock = nuevoStock;
    }
    public void setFechaVencimiento(Date nuevaFechaVencimiento){
        this.fechaVencimiento = nuevaFechaVencimiento;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Producto producto = (Producto) obj;
        return nombreProducto.equalsIgnoreCase(producto.nombreProducto)
                && marcaProducto.equalsIgnoreCase(producto.marcaProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombreProducto.toLowerCase(), marcaProducto.toLowerCase());
    }

    @Override
    public String toString() {
        return nombreProducto;
    }


}