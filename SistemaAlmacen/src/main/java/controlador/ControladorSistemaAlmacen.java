package controlador;

import modelo.*;
import modelo.Interface.IGestorCompras;
import modelo.Interface.ISistemaAlmacen;
import vista.FormularioInicio;

import javax.swing.*;
import java.util.List;

public class ControladorSistemaAlmacen {

    private CarritoDeCompra carritoActual;
    private Usuario usuarioActual;
    private ISistemaAlmacen sistemaAlmacen;
    private IGestorCompras gestorCompras;
    public ControladorSistemaAlmacen() {
        this.sistemaAlmacen=new SistemaAlmacen();
        this.gestorCompras = sistemaAlmacen.getGestorCompras();
    }

    public boolean iniciarSesion(String correo, String clave) {
        Cliente usuarioTemp = new Cliente("Temporal", 0, correo, clave);
        boolean exito = sistemaAlmacen.getGestorUsuario().iniciarSesion(usuarioTemp);

        if (exito) {
            // Recuperamos el cliente real desde el gestor
            usuarioActual = sistemaAlmacen.getGestorUsuario().buscarClientePorCorreo(correo);
        }

        return exito;

    }
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void abrirPanelPrincipal() {
        SwingUtilities.invokeLater(() -> {

            if (carritoActual == null) {
                carritoActual = new CarritoDeCompra((Cliente) usuarioActual);
            }

            FormularioInicio formularioInicio = new FormularioInicio(
                    this,
                    usuarioActual,
                    carritoActual
            );

            formularioInicio.setVisible(true);
        });
    }


    //  Acceso a productos
    public void agregarProducto(Producto p) {
        sistemaAlmacen.getGestorProductos().agregarProducto(p);
        sistemaAlmacen.getGestorProductos().guardarProducto();
    }

    public List<Producto> obtenerProductos() {
        return sistemaAlmacen.getGestorProductos().getProductos();
    }

    public boolean registrarUsuario(String nombre, int edad, String correo, String contrasenia) {
        Cliente nuevo = new Cliente(nombre, edad, correo, contrasenia);
        return sistemaAlmacen.getGestorUsuario().registrarUsuario(nuevo);
    }
    public boolean esEmpleado(String correo) {
        Empleado usuarioTemp = new Empleado("Temporal", 0, correo, "dummy");
        return sistemaAlmacen.getGestorUsuario().esEmpleado(usuarioTemp.getCorreoElectronico());
    }

    public List<String> obtenerFacturas() {
        return sistemaAlmacen.getGestorCompras().getFacturas();
    }

    public ISistemaAlmacen getSistemaAlmacen(){
        return sistemaAlmacen;
    }

    public CarritoDeCompra getCarritoActual() {
        return carritoActual;
    }

    public void setCarritoActual(CarritoDeCompra carrito) {
        this.carritoActual = carrito;
    }
    public IGestorCompras getGestorCompras(){
        return gestorCompras;
    }
}