package vista;

import javax.swing.*;
import java.awt.*;

public class FormularioTarjeta extends JFrame {

    private JComboBox<String> cmbBanco;
    private JComboBox<String> cmbTarjeta;
    private JComboBox<String> cmbCuotas;

    private JLabel lblSubtotal;
    private JLabel lblInteres;
    private JLabel lblTotalFinal;
    private JLabel lblCuotaMensual;

    private double subtotal;
    private double interesAplicado;
    private double totalFinal;
    private int cuotasSeleccionadas;

    private boolean confirmado = false;

    public FormularioTarjeta(double subtotal) {
        this.subtotal = subtotal;

        setTitle("Método de Pago - Tarjeta");
        setSize(450, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // ---------- TITULO ----------
        JLabel titulo = new JLabel("Pago con Tarjeta", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(15));

        // ---------- BANCO ----------
        panel.add(new JLabel("Banco:"));
        cmbBanco = new JComboBox<>(new String[]{
                "Nación", "Provincia", "Santander", "Galicia"
        });
        panel.add(cmbBanco);
        panel.add(Box.createVerticalStrut(10));

        // ---------- TIPO TARJETA ----------
        panel.add(new JLabel("Tipo de Tarjeta:"));
        cmbTarjeta = new JComboBox<>(new String[]{
                "Visa", "Mastercard", "Naranja"
        });
        panel.add(cmbTarjeta);
        panel.add(Box.createVerticalStrut(10));

        // ---------- CUOTAS ----------
        panel.add(new JLabel("Cuotas:"));
        cmbCuotas = new JComboBox<>(new String[]{
                "1", "3", "6", "12"
        });
        panel.add(cmbCuotas);
        panel.add(Box.createVerticalStrut(15));

        // ---------- RESULTADOS ----------
        lblSubtotal = new JLabel();
        lblInteres = new JLabel();
        lblTotalFinal = new JLabel();
        lblCuotaMensual = new JLabel();

        lblSubtotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInteres.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotalFinal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCuotaMensual.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(lblSubtotal);
        panel.add(lblInteres);
        panel.add(lblTotalFinal);
        panel.add(lblCuotaMensual);

        actualizarCalculos();

        cmbCuotas.addActionListener(e -> actualizarCalculos());

        // ---------- BOTONES ----------
        panel.add(Box.createVerticalStrut(20));

        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar");

        btnConfirmar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCancelar.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnConfirmar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });

        btnCancelar.addActionListener(e -> dispose());

        panel.add(btnConfirmar);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnCancelar);

        add(panel, BorderLayout.CENTER);
    }

    private void actualizarCalculos() {
        cuotasSeleccionadas = Integer.parseInt((String) cmbCuotas.getSelectedItem());

        switch (cuotasSeleccionadas) {
            case 1 -> interesAplicado = 0.00;
            case 3 -> interesAplicado = 0.10;
            case 6 -> interesAplicado = 0.20;
            case 12 -> interesAplicado = 0.35;
            default -> interesAplicado = 0.00;
        }

        double montoInteres = subtotal * interesAplicado;
        totalFinal = subtotal + montoInteres;
        double cuotaMensual = totalFinal / cuotasSeleccionadas;

        lblSubtotal.setText("Subtotal: $" + subtotal);
        lblInteres.setText("Interés aplicado: $" + String.format("%.2f", montoInteres));
        lblTotalFinal.setText("Total final: $" + String.format("%.2f", totalFinal));
        lblCuotaMensual.setText("Cuota mensual: $" + String.format("%.2f", cuotaMensual));
    }

    // GETTERS

    public boolean isConfirmado() { return confirmado; }

    public String getBanco() { return (String) cmbBanco.getSelectedItem(); }

    public String getTarjeta() { return (String) cmbTarjeta.getSelectedItem(); }

    public int getCuotas() { return cuotasSeleccionadas; }

    public double getTotalFinal() { return totalFinal; }

    public double getInteres() { return interesAplicado; }
}

