package modelo;

import modelo.Interface.IGestorUsuario;
import modelo.GestorUsuario;
import modelo.Interface.ISistemaAlmacen;
import modelo.SistemaAlmacen;
import modelo.Interface.IGestorProductos;
import modelo.GestorProductos;
import modelo.Interface.IGestorCompras;
import modelo.GestorCompras;
import modelo.Interface.IManejoDeArchivos;
import modelo.ManejoDeArchivos;

public class SistemaAlmacen implements ISistemaAlmacen{
    
    private IGestorUsuario gestorUsuario;
    private IGestorProductos gestorProductos;
    private IGestorCompras gestorCompras;
    private IManejoDeArchivos manejoDeArchivos;

    public SistemaAlmacen() {
        this.gestorUsuario = new GestorUsuario();
        this.gestorProductos = new GestorProductos();
        this.gestorCompras = new GestorCompras();
        this.manejoDeArchivos = new ManejoDeArchivos();
        gestorProductos.cargarProductosDesdeArchivo();
    }
   
    @Override
    public IGestorUsuario getGestorUsuario() {
        return gestorUsuario;
    }

    @Override
    public IGestorProductos getGestorProductos() {
        return gestorProductos;
    }
    
    @Override
    public IGestorCompras getGestorCompras() {
        return gestorCompras;
    }
    @Override
    public IManejoDeArchivos getManejoDeArchivos() {
        return manejoDeArchivos;
    }

}
