package modelo.testeo;

import java.util.Date;

import modelo.Interface.IGestorUsuario;
import modelo.GestorUsuario;
import modelo.Interface.ISistemaAlmacen;
import modelo.SistemaAlmacen;
import modelo.Interface.IGestorProductos;
import modelo.GestorProductos;
import modelo.Interface.IGestorCompras;
import modelo.GestorCompras;
import modelo.Interface.ICarritoDeCompra;
import modelo.CarritoDeCompra;
import modelo.Cliente;
import modelo.Producto;
import modelo.Gaseosa;
import modelo.Interface.IFactura;
import modelo.Factura;
import modelo.MediosDePago;
import modelo.Transferencia;

public class TesteoSistemaAlmacen {
    public static void main (String args[]){
        ISistemaAlmacen sistema = new SistemaAlmacen();

        IGestorUsuario gestorUsuario = sistema.getGestorUsuario();
        IGestorProductos gestorProductos = sistema.getGestorProductos();
        IGestorCompras gestorCompras = sistema.getGestorCompras();

        // 1) Crear y registrar usuario
        Cliente cliente = new Cliente("Gonzalo", 25, "gcalveira@uade.edu.ar", "12345678");

        if (gestorUsuario.registrarUsuario(cliente)) {
            System.out.println("Usuario registrado con éxito");
        } else {
            System.out.println(" El usuario ya existe");
        }

        // 2) Crear productos
        Producto gaseosa1 = new Gaseosa("Gaseosa", 150.0, new Date(125, 11, 31), 50, 1.5, "Coca-Cola");
        Producto gaseosa2 = new Gaseosa("Gaseosa", 150.0, new Date(125, 11, 31), 50, 1.5, "Pepsi");

        gestorProductos.agregarProducto(gaseosa1);
        gestorProductos.agregarProducto(gaseosa2);

        System.out.println("Productos agregados al inventario");

        // 3) Crear carrito
        ICarritoDeCompra carrito = new CarritoDeCompra(cliente);
        carrito.agregarProducto(gaseosa1, 2);
        carrito.agregarProducto(gaseosa2, 3);

        System.out.println(" Carrito creado: " + carrito);

        // 4) Medio de pago
        MediosDePago medioPago = new Transferencia("1122334455");

        // 5) Crear factura
        IFactura factura = new Factura("F001", carrito, medioPago);
        gestorCompras.agregarFactura(factura);

        // 6) Procesar pago
        if (factura.procesarPago()) {
            System.out.println(" Pago procesado");
        }

        // 7) Mostrar factura
        factura.mostrarFactura();

        // 8) Guardar factura en archivo
        if (gestorCompras.guardarCompras()) {
            System.out.println(" Factura guardada en archivo");
        }

        // 9) Actualizar stock
        if (gestorCompras.actualizarStockProducto()) {
            System.out.println(" Stock actualizado correctamente");
        } else {
            System.out.println(" No se pudo actualizar el stock");
        }
    }

}
