package modelo;

import java.util.Date;

public class Carne extends Producto {

    private String tipoCarne;
    private double peso;
    private Date fechaVencimiento;


    public Carne(String nombreProducto, double precioProducto, Date fechaVencimiento,
                 int stock, String marcaProducto, String tipoCarne, double peso) {
        super(nombreProducto, precioProducto, fechaVencimiento, stock, marcaProducto);
        this.tipoCarne = tipoCarne;
        this.peso = peso;
    }



    public String toString() {
        return  getId()+";"+getNombreProducto() + ";" + getPrecioProducto() + ";" +
                getFechaVencimiento().getTime() + ";" + getStock() + ";" +
                getMarcaProducto() + ";" + tipoCarne + ";" + peso;
    }

}