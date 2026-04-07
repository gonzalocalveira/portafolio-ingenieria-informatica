package modelo.Interface;

import java.util.List;

import modelo.Usuario;

public interface IGestorUsuario {
    public boolean registrarUsuario(Usuario usuario);
    public boolean iniciarSesion(Usuario usuario);
    public boolean validarCorreo(Usuario usuario);
    public boolean validarContrasenia(Usuario usuario);
    public boolean buscarUsuarioPorMail(String mail) ;
    public boolean buscarUsuarioPorContrasenia(String contrasenia);
    public List<Usuario> getUsuarios();
    public void agregarUsuarioALaLista(Usuario usuario);
    public boolean esEmpleado(String correo);
    public Usuario buscarClientePorCorreo(String correo);





}
