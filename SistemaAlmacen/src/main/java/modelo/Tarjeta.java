package modelo;

public class Tarjeta extends MediosDePago {
    //atributos
    private String nroTarjeta;
    private String banco;
    //constructor
    public Tarjeta(String nroTarjeta, String banco){
        super("Tarjeta");
        this.nroTarjeta = nroTarjeta;
        this.banco = banco;
    }

    //getters y setter
    public String getNroTarjeta() {
        return nroTarjeta;
    }

    public String getBanco() {
        return banco;
    }

    public  boolean procesarPago(double total){
        if (total <= 0) {
            System.out.println("Pago con tarjeta fallido: monto inválido.");
            return false;
        }
        System.out.println("Pago con tarjeta aprobado.");
        return true;
    }
}
