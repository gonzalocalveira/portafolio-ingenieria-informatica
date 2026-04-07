package modelo.Interface;

import java.time.LocalDateTime;

import modelo.CarritoDeCompra;
import modelo.MediosDePago;

public interface IFactura {

    public String getNroFactura();

    public LocalDateTime getFecha();

    public double calcularTotal();

    public ICarritoDeCompra getCarritoDeCompras();
    public MediosDePago getMediosDePago();

    public double getTotal();

    public void setNroFactura(String nuevoNroFactura);
    public String generarTextoFactura();
    public void setFecha(LocalDateTime nuevaFecha);
    

    public boolean procesarPago();
    public void mostrarFactura();


}
