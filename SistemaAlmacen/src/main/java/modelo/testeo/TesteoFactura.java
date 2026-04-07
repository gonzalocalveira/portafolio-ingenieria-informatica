package modelo.testeo;

import java.util.Date;

import modelo.*;
import modelo.Interface.*;

public class TesteoFactura {

    public static void main(String[] args) {
        Cliente cliente = new Cliente ("Gonzalo",
        25,"gcalveira@uade.edu.ar",
        "12345");

        Gaseosa coca = new Gaseosa("gaseosa", 
        150.0, new Date(125, 11, 31), 50, 1.5, "Coca-Cola");

        ICarritoDeCompra carrito = new CarritoDeCompra(cliente);

        carrito.agregarProducto(coca, 1);

        MediosDePago tarj = new Tarjeta("1234-5678-9012-3456", "Banco Nación");

        MediosDePago trans = new Transferencia("1122334455");

        IFactura factura = new Factura("1", carrito, trans);

        if (factura.procesarPago()) {
            factura.mostrarFactura();
        } else {
            System.out.println("El pago no pudo ser procesado. No se generó la factura.");
        }




    }

}
