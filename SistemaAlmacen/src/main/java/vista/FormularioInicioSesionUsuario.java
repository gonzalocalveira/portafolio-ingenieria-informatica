package vista;

import controlador.ControladorSistemaAlmacen;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FormularioInicioSesionUsuario extends JFrame{
    private JPanel pnlPrincipal;
    private JPanel pnlInicioSesion;
    private JLabel lblUsuario;
    private JLabel lblContrasenia;
    private JTextField JtextUsuario;
    private JTextField textContrasenia;
    private JButton btnIniciarSesion;
    private JButton btnRegistrarse;
    private JLabel lblSistemaAlmacen;
    private JLabel lblMensaje;

    ControladorSistemaAlmacen controladorSistemaAlmacen =new ControladorSistemaAlmacen();

    public FormularioInicioSesionUsuario() {
        iniciarFormulario();

        btnIniciarSesion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                validarCredenciales();
            }
        });

        btnRegistrarse.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
            }
        });
        btnRegistrarse.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FormularioRegistroUsuario registro = new FormularioRegistroUsuario(controladorSistemaAlmacen);
                registro.setVisible(true);
            }
        });
    }

    private void iniciarFormulario(){
        setContentPane(pnlPrincipal); //agrego el Jpanel principal
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //cuando se cierre la ventana, finalice todo
        setSize(600,400); //tamanio
        setLocationRelativeTo(null); //se pone en el centro la ventana
    }

    public void validarCredenciales(){

        String elUsuario= JtextUsuario.getText().trim();
        String laClave= textContrasenia.getText().trim();

        if(elUsuario.isEmpty()|| laClave.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Por favor, complete todos los campos.",
                    "Campos vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }


        if(controladorSistemaAlmacen.iniciarSesion(elUsuario, laClave)){
            lblMensaje.setText("Acceso concedido" );
            if(controladorSistemaAlmacen.esEmpleado(elUsuario)){
                FormularioVerMenuEmpleado menuEmpleado = new FormularioVerMenuEmpleado(controladorSistemaAlmacen);
                menuEmpleado.setVisible(true);
            }else {
                controladorSistemaAlmacen.abrirPanelPrincipal();

            }this.dispose();
        }
        else{
            lblMensaje.setText("ERROR. Volver a ingresar los datos");
        }
    }
}
