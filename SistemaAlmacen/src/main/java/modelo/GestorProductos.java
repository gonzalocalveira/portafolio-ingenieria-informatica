package modelo;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.ParseException;

import modelo.Interface.IGestorProductos;
import modelo.Interface.IManejoDeArchivos;

public class GestorProductos implements IGestorProductos {

    private List<Producto> productos;
    private IManejoDeArchivos manejoDeArchivos;
    private final String ARCHIVO_PRODUCTOS="productos.txt";

    public GestorProductos(){
        productos= new ArrayList<Producto>();
        manejoDeArchivos= new ManejoDeArchivos();

        cargarProductosDesdeArchivo();


    }

    @Override
    public void agregarProducto(Producto p){
        productos.add(p);
    }

    @Override
    public boolean actualizarStockProducto(Producto p, int cantidadADescontar){
        int stockProductoActual=p.getStock();


        String lineavieja=p.toString();

        p.setStock(stockProductoActual);

        String lineaact=p.toString();

        if(p.getStock()==0) {

            manejoDeArchivos.actualizarLineaProducto(ARCHIVO_PRODUCTOS, String.valueOf(p.getId()), lineaact, true);
            return true;
        }
        else{
            manejoDeArchivos.actualizarLineaProducto(ARCHIVO_PRODUCTOS, String.valueOf(p.getId()), lineaact,false);
            return true;


        }

    }

    @Override
    public List<Producto> getProductos(){
        return productos;
    }

    @Override
    public boolean guardarProducto(){
        boolean exito = true;

        for (Producto p : productos) {
            // Evita duplicados
            if (!manejoDeArchivos.leer(ARCHIVO_PRODUCTOS, p.getNombreProducto())) {
                boolean resultado = manejoDeArchivos.escribir(ARCHIVO_PRODUCTOS, p, true);
                if (!resultado) {
                    exito = false;
                }
            }
        }
        return exito;
    }

    @Override
    public Producto buscarProductoPorNombre(String nombreProducto){
        for(Producto p: productos){
            if(p.getNombreProducto().equalsIgnoreCase(nombreProducto)){
                return p;
            }
        }
        return null;
    }

    @Override
    public boolean eliminarProducto(Producto p){
        if(productos.contains(p)){
            productos.remove(p);
            return true;
        }
        return false;
    }
    @Override
    public void cargarProductosDesdeArchivo() {
        List<String> lineas = manejoDeArchivos.leerTodo(ARCHIVO_PRODUCTOS);
        productos.clear();

        for (String linea : lineas) {
            try {
                String[] partes = linea.split(";");
                if (partes.length < 6) continue;

                String id = partes[0];
                String nombre = partes[1];
                double precio = Double.parseDouble(partes[2]);
                Date fecha = new Date(Long.parseLong(partes[3]));
                int stock = Integer.parseInt(partes[4]);
                String marca = partes[5];

                switch (nombre.toLowerCase()) {

                    case "gaseosa":
                        double litros = Double.parseDouble(partes[6]);
                        productos.add(new Gaseosa(
                                nombre, precio, fecha, stock, litros, marca
                        ));
                        break;

                    case "carne":
                        String tipoCarne = partes[6];
                        double peso = Double.parseDouble(partes[7]);
                        productos.add(new Carne(
                                nombre, precio, fecha, stock, marca, tipoCarne, peso
                        ));
                        break;

                    case "queso":
                        String tipoQueso = partes[6];
                        productos.add(new Queso(
                                nombre, precio, fecha, stock, marca, tipoQueso
                        ));
                        break;
                }

            } catch (Exception e) {
                System.err.println("Error al leer producto: " + e.getMessage());
            }
        }
    }
    @Override
    public void actualizarUltimoId() {
        long max = 0;

        for (Producto p : productos) {
            if (p.getId() > max) {
                max = p.getId();
            }
        }

        Producto.setUltimoId(max + 1);
    }

}
