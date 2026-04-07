package modelo.testeo;

import java.util.Date;

import modelo.*;
import modelo.Interface.IGestorProductos;
public class TesteoGestorProductos {
    public static void main (String args[]){
        IGestorProductos gestor= new GestorProductos();
        Queso queso1= new Queso("queso", 500.0,
         new Date(125, 10, 15),10,
            "La Paulina","Pategras");
        gestor.agregarProducto(queso1);

        Carne c1= new Carne("carneVaca",50,
                new Date(124,10,15),10,"Coto","vaca",10);
        gestor.agregarProducto(c1);
        if(gestor.guardarProducto()){
            System.out.println("Producto guardado con exito");
        }
        else{
            System.out.println("Error al guardar el producto");


    


        }
        
        System.out.println(gestor.buscarProductoPorNombre(queso1.getNombreProducto()));
        gestor.eliminarProducto(queso1);
        System.out.println(gestor.getProductos());


    }
}
