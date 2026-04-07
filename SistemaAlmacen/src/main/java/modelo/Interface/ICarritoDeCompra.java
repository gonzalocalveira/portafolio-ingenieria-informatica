package modelo.Interface;

import java.util.HashMap;

import modelo.Cliente;
import modelo.Producto;
import modelo.Usuario;

public interface ICarritoDeCompra {
      public double getPrecioTotal();
    public Cliente getCliente();
    public int validarTipoPago();
    public void agregarProducto(Producto producto, int cantidad);

    public void eliminarProducto(Producto producto, int cantidad);
    public HashMap<Producto, Integer> getProductos();
    public void setUsuario(Usuario usuario);


}
