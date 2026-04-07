package modelo.Interface;

import java.util.*;

public interface IManejoDeArchivos {
    public boolean leer(String nombreArchivo, String datoBuscado);

    public <T> boolean escribir(String nombreArchivo, T objeto, boolean append);

    public String buscarFacturaCliente(String nombreArchivo, String correoCliente);

    public boolean validarLogin(String archivosUsuarios, String correoElectronico, String contrasenia);

    public List<String> leerTodo(String nombreArchivo);

    public boolean actualizarLineaProducto(String nombreArchivo, String idProducto, String lineaNueva,boolean eliminar);

}