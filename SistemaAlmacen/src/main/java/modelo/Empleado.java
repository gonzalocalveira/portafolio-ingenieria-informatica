package modelo;

public class Empleado extends Usuario{
    
    
    public  Empleado(String nombre, int edad, String correoElectronico, 
    String contrasenia){
        super(nombre, edad,correoElectronico,contrasenia);

    }

    public String toString(){
        return getNombre() + ";" + getEdad() +
        ";" + getCorreoElectronico() +
        ";" + getContrasenia();
    }
    
}
