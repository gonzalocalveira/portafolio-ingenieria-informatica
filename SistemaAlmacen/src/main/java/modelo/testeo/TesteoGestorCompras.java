package modelo.testeo;

import java.util.Date;

import modelo.*;
import modelo.Interface.*;
public class TesteoGestorCompras {
    public static void main(String[] args) {


        IGestorCompras gestorCompras= new GestorCompras();

        Cliente cliente = new Cliente ("Gonzalo", 25,"gcalveira@uade.edu.ar","12345");

         Gaseosa coca = new Gaseosa("gaseosa", 
        150.0, new Date(125, 11, 31), 50,
        1.5, "Coca-Cola");

        ICarritoDeCompra carrito = new CarritoDeCompra(cliente);

        carrito.agregarProducto(coca, 1);

        MediosDePago tarj = new Tarjeta("1234-5678-9012-3456", "Banco Nación");

        MediosDePago trans = new Transferencia("1122334455");

        IFactura factura = new Factura("80", carrito, trans);
        gestorCompras.agregarFactura(factura);

        if(gestorCompras.guardarCompras()){
            System.out.println("Compra guardada con exito");
        }
        else{
            System.out.println("Error al guardar la compra");
        }
        
        System.out.println(gestorCompras.consultarCompraPorCliente(cliente));
   
    }

}
