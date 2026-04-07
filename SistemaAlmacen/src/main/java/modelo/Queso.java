package modelo;

import java.util.Date;



public class Queso  extends Producto  {
    private String tipoQueso;


    public Queso(String nombreProducto, double precioProducto, Date fechaVencimiento,
                 int stock, String marcaProducto, String tipoQueso)
    {
        super(nombreProducto,precioProducto,
                fechaVencimiento,stock,marcaProducto);
        this.tipoQueso = tipoQueso;
    }

    @Override
    public String toString() {
        return getId()+";"+getNombreProducto() + ";" + getPrecioProducto() + ";" +
                getFechaVencimiento().getTime() + ";" + getStock() + ";" +
                getMarcaProducto() + ";" + tipoQueso;
    }

}