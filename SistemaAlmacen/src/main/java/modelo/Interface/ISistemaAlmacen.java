package modelo.Interface;

public interface ISistemaAlmacen {
    public IGestorUsuario getGestorUsuario();
    public IGestorProductos getGestorProductos();
    public IGestorCompras getGestorCompras();
    public IManejoDeArchivos getManejoDeArchivos();
}
