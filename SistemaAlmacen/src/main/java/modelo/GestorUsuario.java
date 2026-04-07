package modelo;

import java.util.*;

import modelo.Interface.IGestorUsuario;
import modelo.Interface.IManejoDeArchivos;

public class GestorUsuario implements IGestorUsuario {

    private List<Usuario> usuarios;
    private final String ARCHIVOS_USUARIOS = "usuarios.txt";
    private IManejoDeArchivos manejoDeArchivos;

    public GestorUsuario() {
        this.usuarios = new ArrayList<>();
        this.manejoDeArchivos = new ManejoDeArchivos();
    }

    @Override
    public boolean buscarUsuarioPorMail(String mail) {
        return manejoDeArchivos.leer(ARCHIVOS_USUARIOS, mail);
    }

    @Override
    public boolean buscarUsuarioPorContrasenia(String contrasenia) {
        return manejoDeArchivos.leer(ARCHIVOS_USUARIOS, contrasenia);
    }

    @Override
    public boolean registrarUsuario(Usuario usuario) {
        if (!buscarUsuarioPorMail(usuario.getCorreoElectronico())) {
            if (validarCorreo(usuario) && validarContrasenia(usuario)) {
                if (esEmpleado(usuario.getCorreoElectronico())) {
                    usuario = new Empleado(
                            usuario.getNombre(),
                            usuario.getEdad(),
                            usuario.getCorreoElectronico(),
                            usuario.getContrasenia()
                    );
                    }
                usuarios.add(usuario);
                return manejoDeArchivos.escribir(ARCHIVOS_USUARIOS, usuario, true);
            }
        }
        return false;
    }

    @Override
    public void agregarUsuarioALaLista(Usuario usuario) {
        usuarios.add(usuario);
    }

    @Override
    public boolean iniciarSesion(Usuario usuario) {
        return manejoDeArchivos.validarLogin(ARCHIVOS_USUARIOS,
                usuario.getCorreoElectronico(),
                usuario.getContrasenia());

    }

    @Override
    public boolean validarCorreo(Usuario usuario) {
        String correo = usuario.getCorreoElectronico();
        return correo.contains("@") && correo.contains(".");
    }
    @Override
    public boolean esEmpleado(String correo) {
        // Devuelve true si el correo termina en @sistemaalmacen.com
        return correo.toLowerCase().endsWith("@sistemaalmacen.com");
    }


    @Override
    public boolean validarContrasenia(Usuario usuario) {
        return usuario.getContrasenia().length() <= 8;
    }

    @Override
    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    @Override
    public Usuario buscarClientePorCorreo(String correo) {
        List<String> lineas = manejoDeArchivos.leerTodo(ARCHIVOS_USUARIOS);
        if (lineas == null) return null;

        for (String linea : lineas) {
            String[] partes = linea.split(";");
            if (partes.length >= 4) {
                String nombre = partes[0];
                int edad = Integer.parseInt(partes[1]);
                String correoArchivo = partes[2];
                String contrasenia = partes[3];

                if (correoArchivo.equalsIgnoreCase(correo)) {

                    if (esEmpleado(correoArchivo)) {
                        return new Empleado(nombre, edad, correoArchivo, contrasenia);
                    } else {
                        return new Cliente(nombre, edad, correoArchivo, contrasenia);
                    }
                }
            }
        }
        return null;
    }


}
