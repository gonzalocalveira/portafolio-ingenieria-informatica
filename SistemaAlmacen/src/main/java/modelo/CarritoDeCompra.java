package modelo;

import java.util.*;
import modelo.Interface.ICarritoDeCompra;

public class CarritoDeCompra implements ICarritoDeCompra {

    private Producto producto;
    private List<MediosDePago> mediosDePago;
    private HashMap<Producto, Integer> productos;
    private Cliente cliente;

    public CarritoDeCompra  (Cliente cliente){
        this.mediosDePago = new ArrayList<MediosDePago>();
        this.productos = new HashMap<Producto, Integer>();
        this.cliente = cliente;
    }

    //getters, setters y metodos


    @Override
    public double getPrecioTotal(){
       double total = 0;
       for (Map.Entry<Producto, Integer> p: productos.entrySet()){
              total+=p.getKey().getPrecioProducto()*p.getValue();
       }
       return total;
    }

    @Override
    public Cliente getCliente(){
        return cliente;
    }

    @Override
    public int validarTipoPago(){
        return 0;
    }

    @Override
    public void agregarProducto(Producto producto, int cantidad){
        if (productos.containsKey(producto)){
            int cantidadActual= productos.get(producto);
            productos.put(producto, cantidad + cantidadActual);
        } else {
            productos.put(producto, cantidad);
        }
    }

    @Override
    public void eliminarProducto(Producto producto, int cantidad){
        if(productos.containsKey(producto)){
            int cantidadActual= productos.get(producto);
            if(cantidadActual <=cantidad){
                productos.remove(producto);
            }
            else{
                productos.put(producto, cantidadActual- cantidad);
            }
            
        }
    }

    @Override
    public HashMap<Producto, Integer> getProductos(){
        return productos;
    }

  public String toString(){
    String mostrar= "Carrito de compras de: "+ cliente.getNombre()+
    "\nProductos   Marca   Cantidad    Precio \n";

    for(Map.Entry<Producto,Integer> p: productos.entrySet()){
        mostrar+= p.getKey().getNombreProducto()+"   "+p.getKey().getMarcaProducto()+"     "+ 
        p.getValue() + "       $"+ p.getKey().getPrecioProducto()*p.getValue()+"\n";
    }

    mostrar+= "\nPrecio Total: "+ getPrecioTotal();
    return mostrar;
  }

    @Override
    public void setUsuario(Usuario usuario) {
        if (usuario instanceof Cliente) {
            this.cliente = (Cliente) usuario;
        } else {
            // Si no es cliente, se crea uno temporal para conservar los datos
            this.cliente = new Cliente(
                    usuario.getNombre(),
                    usuario.getEdad(),
                    usuario.getCorreoElectronico(),
                    usuario.getContrasenia()
            );
        }
    }

}