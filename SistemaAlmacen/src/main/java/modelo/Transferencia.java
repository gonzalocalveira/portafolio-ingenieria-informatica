package modelo;

public class Transferencia extends MediosDePago{
    //atributos
    private String cbu;
    //constructor
    public Transferencia(String cbu){
        super("Transferencia");
        this.cbu = cbu;
    }

    //getters, setters y metodos

    public String getCbu() {
        return cbu;
    }

    public boolean procesarPago(double total) {
        if (total <= 0) {
            System.out.println("Pago por transferencia fallido: monto inválido.");
            return false;
        }
        System.out.println("Pago por transferencia aprobado.");
        return true;
    }
}
