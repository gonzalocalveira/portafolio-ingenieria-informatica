package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class FormularioTransferencia extends JFrame {

    private boolean confirmado = false;

    public FormularioTransferencia(double total) {

        setTitle("Pago por Transferencia");
        setSize(400, 260);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridLayout(6, 1, 5, 5));

        // Etiquetas con los datos fijos del comercio
        JLabel lblAlias = new JLabel("Alias: almacen.pagos");
        JLabel lblCBU = new JLabel("CBU: 0000003100038971234567");
        JLabel lblTitular = new JLabel("Titular: Sistema Almacen S.A");
        JLabel lblTotal = new JLabel("Monto a pagar: $" + String.format("%.2f", total));

        JButton btnCopiar = new JButton("Copiar alias");
        JButton btnConfirmar = new JButton("Ya transferí");
        JButton btnCancelar = new JButton("Cancelar");

        // Copiar alias al portapapeles
        btnCopiar.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection("almacen.pagos"), null);
            JOptionPane.showMessageDialog(this, "Alias copiado");
        });

        // Botón para confirmar pago
        btnConfirmar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });

        // Botón cancelar
        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });

        add(lblAlias);
        add(lblCBU);
        add(lblTitular);
        add(lblTotal);
        add(btnCopiar);

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);

        add(panelBotones);
    }

    public boolean isConfirmado() {
        return confirmado;
    }
}
