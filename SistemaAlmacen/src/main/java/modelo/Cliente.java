package modelo;

public class Cliente extends Usuario{
    
    
    public  Cliente(String nombre, int edad, String correoElectronico, 
    String contrasenia){
        super(nombre, edad,correoElectronico,contrasenia);

    }

    public String toString(){
        return getNombre() + ";" + getEdad() + ";" + getCorreoElectronico() + ";" + getContrasenia();
    }



}
