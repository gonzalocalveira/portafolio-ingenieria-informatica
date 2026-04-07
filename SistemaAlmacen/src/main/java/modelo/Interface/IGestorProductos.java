package modelo.Interface;

import java.util.List;

import modelo.Producto;
public interface IGestorProductos {
    public void agregarProducto(Producto p);
    public boolean actualizarStockProducto(Producto p, int cantidadADescontar);
    public List<Producto> getProductos();
    public boolean guardarProducto();
    public Producto buscarProductoPorNombre(String nombreProducto);
    public boolean eliminarProducto(Producto p);
    public void cargarProductosDesdeArchivo();
    public void actualizarUltimoId();
}