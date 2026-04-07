package modelo;

import java.util.*;


import modelo.Interface.*;
public class GestorCompras implements IGestorCompras {
    private List<IFactura> facturas;

    private IManejoDeArchivos manejoDeArchivos;
    private final String ARCHIVO_COMPRA="compra.txt";
    private IGestorProductos gestorProductos;




    
    public GestorCompras(){
        facturas= new ArrayList<IFactura>();
        manejoDeArchivos= new ManejoDeArchivos();
        gestorProductos= new GestorProductos();

    }
    
    @Override
    public boolean guardarCompras(){
        for(IFactura f: facturas){
            if(!manejoDeArchivos.leer(ARCHIVO_COMPRA, f.getNroFactura())){
                return manejoDeArchivos.escribir(ARCHIVO_COMPRA, f, true);
             }
            
        }
        return false;
    }

    @Override
    public void agregarFactura(IFactura factura){
        facturas.add(factura);
    }

    @Override
    public boolean actualizarStockProducto(){

         for (IFactura f : facturas) {

        ICarritoDeCompra carrito = f.getCarritoDeCompras();
        HashMap<Producto, Integer> productosDelCarrito = carrito.getProductos();

        for (Map.Entry<Producto, Integer> entry : productosDelCarrito.entrySet()) {
            Producto producto = entry.getKey();
            int cantidad = entry.getValue();

            gestorProductos.actualizarStockProducto(producto, cantidad);
            }
        }
        return true;
    }

    
    @Override
    public String consultarCompraPorCliente(Usuario u){
       return manejoDeArchivos.buscarFacturaCliente(ARCHIVO_COMPRA, u.getNombre());
    }
    @Override
    public List<String> getFacturas() {
        // Leemos todas las líneas del archivo de compras
        return manejoDeArchivos.leerTodo(ARCHIVO_COMPRA);
    }


}

