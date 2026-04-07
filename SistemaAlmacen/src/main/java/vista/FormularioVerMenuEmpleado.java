package vista;

import controlador.ControladorSistemaAlmacen;
import modelo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class FormularioVerMenuEmpleado extends JFrame {
    private JPanel panelPrincipal;
    private JLabel lblTitulo;
    private JButton verProductosButton;
    private JButton agregarProductoButton;
    private JButton verFacturasButton;
    private JButton cerrarSesionButton;

    private ControladorSistemaAlmacen controlador;

    public FormularioVerMenuEmpleado(ControladorSistemaAlmacen controlador) {
        this.controlador = controlador;

        setContentPane(panelPrincipal);
        setTitle("Menú del Empleado");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panelPrincipal.setBackground(new Color(228, 0, 43)); // Rojo fuerte

        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);

        cerrarSesionButton = new JButton("Cerrar Sesión");


        //  Botones estilo moderno
        JButton[] botones = {verProductosButton, agregarProductoButton, verFacturasButton, cerrarSesionButton};

        for (JButton b : botones) {
            b.setBackground(Color.WHITE);
            b.setForeground(new Color(228, 0, 43));
            b.setFont(new Font("Segoe UI", Font.BOLD, 18));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(250, 45));
        }

        // Centramos visualmente
        panelPrincipal.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        panelPrincipal.add(lblTitulo, gbc);
        gbc.gridy++;
        panelPrincipal.add(verProductosButton, gbc);
        gbc.gridy++;
        panelPrincipal.add(agregarProductoButton, gbc);
        gbc.gridy++;
        panelPrincipal.add(verFacturasButton, gbc);
        gbc.gridy++;
        panelPrincipal.add(cerrarSesionButton, gbc);


        agregarListeners();
    }

    private void agregarListeners() {

        //  Agregar producto
        agregarProductoButton.addActionListener(e -> agregarProducto());

        //  Ver productos
        verProductosButton.addActionListener(e -> verProductos());

        //  Ver facturas
        verFacturasButton.addActionListener(e -> verFacturas());

        cerrarSesionButton.addActionListener(e -> cerrarSesion());

    }

    // ===================== MÉTODOS =====================

    private void agregarProducto() {
        try {
            String nombre = pedirDato("Ingrese nombre del producto:");
            String marca = pedirDato("Ingrese marca:");
            double precio = Double.parseDouble(pedirDato("Ingrese precio:"));
            int stock = Integer.parseInt(pedirDato("Ingrese stock:"));
            String tipo = pedirDato("Tipo de producto (gaseosa/queso/carne):").toLowerCase();

            Producto nuevo = null;

            switch (tipo.toLowerCase()) {
                case "gaseosa":
                    double litros = Double.parseDouble(JOptionPane.showInputDialog("Ingrese litros:"));
                    nuevo = new Gaseosa(nombre, precio, new Date(), stock, litros, marca);

                    break;
                case "queso":
                    String tipoQueso = JOptionPane.showInputDialog("Ingrese tipo de queso:");
                    nuevo = new Queso(nombre, precio, new Date(), stock, marca, tipoQueso);
                    break;
                case "carne":
                    String tipoCarne = JOptionPane.showInputDialog("Ingrese tipo de carne:");
                    double peso = Double.parseDouble(JOptionPane.showInputDialog("Ingrese peso (kg):"));
                    nuevo = new Carne(nombre, precio, new Date(), stock, marca, tipoCarne, peso);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Tipo de producto inválido.");
                    return;
            }

            controlador.agregarProducto(nuevo);
            JOptionPane.showMessageDialog(this, " Producto agregado correctamente");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Error al agregar producto: " + ex.getMessage());
        }
    }

    private void verProductos() {
        List<Producto> lista = controlador.obtenerProductos();

        if (lista == null || lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos cargados.");
            return;
        }

        StringBuilder sb = new StringBuilder(" Productos registrados:\n\n");
        for (Producto p : lista) {
            sb.append(p.toString()).append("\n");
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new java.awt.Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scroll, "Listado de Productos", JOptionPane.INFORMATION_MESSAGE);
    }

    private void verFacturas() {
        List<String> facturas = controlador.obtenerFacturas();

        if (facturas == null || facturas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay facturas registradas.");
            return;
        }

        StringBuilder sb = new StringBuilder("Facturas registradas:\n\n");
        for (String f : facturas) {
            sb.append(f).append("\n");
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new java.awt.Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scroll, "Listado de Facturas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Desea cerrar sesión?",
                "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            this.dispose(); // cierra la ventana actual

            new FormularioInicioSesionUsuario().setVisible(true);
        }
    }
    private String pedirDato(String mensaje) {
        String input = JOptionPane.showInputDialog(mensaje);
        if (input == null) {  // usuario apretó cancelar
            throw new RuntimeException("Operación cancelada por el usuario");
        }
        return input;
    }

}
