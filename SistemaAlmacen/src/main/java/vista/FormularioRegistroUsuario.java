package vista;

import controlador.ControladorSistemaAlmacen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FormularioRegistroUsuario extends JFrame {

    private JPanel panelPrincipal;
    private JLabel lbltituloRegistro;
    private JLabel lblNombre;
    private JLabel lblEdad;
    private JLabel lblCorreo;
    private JLabel lblContrasenia;
    private JTextField textFieldNombre;
    private JTextField textFieldEdad;
    private JTextField textFieldCorreo;
    private JTextField textFieldContrasenia;
    private JButton buttonRegistrarse;

    private ControladorSistemaAlmacen controladorSistemaAlmacen;

    public FormularioRegistroUsuario(ControladorSistemaAlmacen controladorSistemaAlmacen){
        this.controladorSistemaAlmacen = controladorSistemaAlmacen;

        iniciarFormulario();
        aplicarEstilos();

        buttonRegistrarse.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                registrarUsuario();
            }
        });
    }

    private void iniciarFormulario() {
        setContentPane(panelPrincipal);
        setTitle("Registro de Usuario");
        setSize(600, 420);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void aplicarEstilos() {

        // Fondo general
        panelPrincipal.setBackground(Color.decode("#fff5f5"));

        // Título
        lbltituloRegistro.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lbltituloRegistro.setForeground(Color.decode("#D93025"));
        lbltituloRegistro.setHorizontalAlignment(SwingConstants.CENTER);

        // Etiquetas
        JLabel[] labels = { lblNombre, lblEdad, lblCorreo, lblContrasenia };
        for (JLabel lbl : labels) {
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(Color.decode("#D93025"));
        }

        // Campos de texto
        JTextField[] campos = { textFieldNombre, textFieldEdad, textFieldCorreo, textFieldContrasenia };
        for (JTextField txt : campos) {
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txt.setBackground(Color.WHITE);
            txt.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#f2b8b5"), 2),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
        }

        // Botón
        buttonRegistrarse.setFocusPainted(false);
        buttonRegistrarse.setBackground(Color.decode("#D93025"));
        buttonRegistrarse.setForeground(Color.WHITE);
        buttonRegistrarse.setFont(new Font("Segoe UI", Font.BOLD, 16));
        buttonRegistrarse.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        buttonRegistrarse.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonRegistrarse.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                buttonRegistrarse.setBackground(Color.decode("#b3221c"));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                buttonRegistrarse.setBackground(Color.decode("#D93025"));
            }
        });
    }

    private void registrarUsuario() {
        String nombre = textFieldNombre.getText().trim();
        String edadTexto = textFieldEdad.getText().trim();
        String correo = textFieldCorreo.getText().trim();
        String contrasenia = textFieldContrasenia.getText().trim();

        if (nombre.isEmpty() || edadTexto.isEmpty() || correo.isEmpty() || contrasenia.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete todos los campos.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int edad = Integer.parseInt(edadTexto);

            boolean registrado = controladorSistemaAlmacen.registrarUsuario(nombre, edad, correo, contrasenia);

            if (registrado) {
                JOptionPane.showMessageDialog(this,
                        "Usuario registrado con éxito.",
                        "Registro exitoso",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "El correo ya se encuentra registrado.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "La edad debe ser un número válido.",
                    "Error en edad",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
