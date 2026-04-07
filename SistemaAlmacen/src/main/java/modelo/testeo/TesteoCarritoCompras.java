package modelo.testeo;

import modelo.*;

import java.util.Date;
import modelo.Interface.*;

public class TesteoCarritoCompras {
        static public void main (String args[]){
            Cliente cliente1 = new Cliente ("Gonzalo",
             25,"gcalveira@uade.edu.ar",
             "12345");


            Gaseosa gaseosa1= new Gaseosa("gaseosa", 
            150.0, new Date(125, 11, 31), 50,
             1.5, "Coca-Cola");

             Gaseosa gaseosa2= new Gaseosa("gaseosa", 
            150.0, new Date(125, 11, 31), 50,
             1.5, "Pepsi");
             Gaseosa gaseosa3= new Gaseosa("gaseosa", 
            150.0, new Date(125, 11, 31), 50,
             1.5, "Coca-Cola");
            
            ICarritoDeCompra carrito= new CarritoDeCompra(cliente1);
            carrito.agregarProducto(gaseosa1, 1);
            carrito.agregarProducto(gaseosa2, 2);
            carrito.agregarProducto(gaseosa3, 3);
            System.out.println(carrito);
            carrito.eliminarProducto(gaseosa1,2);
            System.out.println(carrito);

        
        
        }

}
