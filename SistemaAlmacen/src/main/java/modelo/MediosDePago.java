package modelo;

public abstract class MediosDePago {

    private String tipoPago;

    public MediosDePago(String tipoPago){
        this.tipoPago=tipoPago;
    }

    //getters y setters

    public String getTipoPago(){
        return tipoPago;
    }

    public void setTipoPago(String nuevoTipoPago){
        this.tipoPago=nuevoTipoPago;
    }

    public abstract boolean procesarPago(double total);


    
}
