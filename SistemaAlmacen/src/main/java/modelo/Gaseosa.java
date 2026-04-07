package modelo;

import java.util.Date;

public class Gaseosa extends Producto {
    //atributos
    private String marca;
    private double litros;

    public Gaseosa(String nombreProducto, double precioProducto,Date fechaVencimiento,
                   int stock, double litros, String marcaProducto){
        super(nombreProducto,precioProducto,
                fechaVencimiento,stock,marcaProducto);
        this.litros = litros;
    }



    @Override
    public String toString() {
        return  getId()+";"+getNombreProducto() + ";" + getPrecioProducto() + ";" +
                getFechaVencimiento().getTime() + ";" + getStock() + ";" +
                getMarcaProducto() + ";" + litros;
    }

}