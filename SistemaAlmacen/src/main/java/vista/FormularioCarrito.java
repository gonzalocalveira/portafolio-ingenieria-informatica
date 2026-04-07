package vista;

import controlador.ControladorSistemaAlmacen;
import modelo.*;
import modelo.Interface.ICarritoDeCompra;
import servicio.EnviadorEmail;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Map;

public class FormularioCarrito extends JFrame {

    private JPanel pnlPrincipal;
    private JTable tablaCarrito;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotal;
    private JComboBox<String> cmbMetodoPago;
    private JPanel pnlTransferencia;
    private JTextField txtCBU;
    private JButton btnVolver;

    private ICarritoDeCompra carrito;
    private ControladorSistemaAlmacen controlador;
    public FormularioCarrito(ICarritoDeCompra carrito, ControladorSistemaAlmacen controlador) {
        this.carrito = carrito;
        this.controlador = controlador;

        setTitle("Carrito de Compras");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        iniciarComponentes();
        setVisible(true);
    }

    private void iniciarComponentes() {
        pnlPrincipal = new JPanel(new BorderLayout(10, 10));
        pnlPrincipal.setBackground(Color.decode("#fff5f5"));
        pnlPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(pnlPrincipal);

        JLabel lblTitulo = new JLabel("Carrito de Compras", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.decode("#D93025"));
        pnlPrincipal.add(lblTitulo, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Precio Unitario", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaCarrito = new JTable(modeloTabla);
        tablaCarrito.setRowHeight(30);
        tablaCarrito.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaCarrito.setBackground(Color.WHITE);
        tablaCarrito.setGridColor(Color.decode("#f0f0f0"));
        tablaCarrito.setShowHorizontalLines(true);
        tablaCarrito.setShowVerticalLines(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaCarrito.getColumnCount(); i++) {
            tablaCarrito.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = tablaCarrito.getTableHeader();
        header.setBackground(Color.decode("#D93025"));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setOpaque(true);
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(tablaCarrito);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#f2b8b5"), 2),
                BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        scroll.getViewport().setBackground(Color.decode("#fff5f5"));
        pnlPrincipal.add(scroll, BorderLayout.CENTER);

        cargarProductosCarrito();

        JPanel pnlInferior = new JPanel();
        pnlInferior.setLayout(new BoxLayout(pnlInferior, BoxLayout.Y_AXIS));
        pnlInferior.setBackground(Color.decode("#fff5f5"));
        pnlInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // ---- Panel de edición del carrito ----
        JPanel pnlEdicion = new JPanel();
        pnlEdicion.setBackground(Color.decode("#fff5f5"));

        JButton btnRestar = new JButton(" - ");
        JButton btnEliminar = new JButton("Eliminar");

        btnRestar.addActionListener(e -> {
            int fila = tablaCarrito.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nombreProducto = (String) modeloTabla.getValueAt(fila, 0);

            // Busco el producto real
            Producto seleccionado = carrito.getProductos().keySet().stream()
                    .filter(p -> p.getNombreProducto().equals(nombreProducto))
                    .findFirst()
                    .orElse(null);

            if (seleccionado == null) return;

            int cantidadActual = carrito.getProductos().get(seleccionado);

            if (cantidadActual > 1) {
                carrito.getProductos().put(seleccionado, cantidadActual - 1);
            } else {
                carrito.getProductos().remove(seleccionado);
            }

            cargarProductosCarrito();
            lblTotal.setText("Total: $" + calcularTotal());
        });

        btnEliminar.addActionListener(e -> {
            int fila = tablaCarrito.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nombreProducto = (String) modeloTabla.getValueAt(fila, 0);

            // Busco el producto real
            Producto seleccionado = carrito.getProductos().keySet().stream()
                    .filter(p -> p.getNombreProducto().equals(nombreProducto))
                    .findFirst()
                    .orElse(null);

            if (seleccionado == null) return;

            carrito.getProductos().remove(seleccionado);

            cargarProductosCarrito();
            lblTotal.setText("Total: $" + calcularTotal());
        });


        btnRestar.setBackground(Color.decode("#D93025"));
        btnRestar.setForeground(Color.WHITE);
        btnEliminar.setBackground(Color.decode("#D93025"));
        btnEliminar.setForeground(Color.WHITE);

        pnlEdicion.add(btnRestar);
        pnlEdicion.add(btnEliminar);

        pnlInferior.add(Box.createVerticalStrut(10));
        pnlInferior.add(pnlEdicion);


        lblTotal = new JLabel("Total: $" + calcularTotal());
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(Color.decode("#D93025"));
        lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlInferior.add(lblTotal);
        pnlInferior.add(Box.createVerticalStrut(10));

        JLabel lblMetodo = new JLabel("Seleccione método de pago:");
        lblMetodo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMetodo.setForeground(Color.decode("#D93025"));
        lblMetodo.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlInferior.add(lblMetodo);

        cmbMetodoPago = new JComboBox<>(new String[]{"","Tarjeta", "Transferencia"});
        cmbMetodoPago.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbMetodoPago.setMaximumSize(new Dimension(300, 30));
        cmbMetodoPago.setAlignmentX(Component.CENTER_ALIGNMENT);
        cmbMetodoPago.setSelectedIndex(0);
        pnlInferior.add(cmbMetodoPago);
        pnlInferior.add(Box.createVerticalStrut(10));

        pnlTransferencia = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlTransferencia.setBackground(Color.decode("#fff5f5"));
        pnlTransferencia.setVisible(false);
        pnlTransferencia.setPreferredSize(new Dimension(0, 0));

        JLabel lblCBU = new JLabel("CBU:", SwingConstants.RIGHT);
        lblCBU.setForeground(Color.decode("#D93025"));
        txtCBU = new JTextField();
        pnlTransferencia.add(lblCBU);
        pnlTransferencia.add(txtCBU);
        pnlInferior.add(pnlTransferencia);
        pnlInferior.add(Box.createVerticalStrut(10));

        cmbMetodoPago.addActionListener(e -> {
            String metodo = (String) cmbMetodoPago.getSelectedItem();

            // Guardamos antes, por si hay que restaurarlo
            int indiceSeleccionado = cmbMetodoPago.getSelectedIndex();

            if ("Transferencia".equals(metodo)) {

                FormularioTransferencia ventana = new FormularioTransferencia(calcularTotal());
                ventana.setVisible(true);

                if (ventana.isConfirmado()) {
                    JOptionPane.showMessageDialog(this, "Pago confirmado por transferencia.");
                } else {
                    // Restauramos la selección anterior: "Transferencia"
                    cmbMetodoPago.setSelectedIndex(indiceSeleccionado);
                }
            }

            if ("Tarjeta".equals(metodo)) {

                FormularioTarjeta ventana = new FormularioTarjeta(calcularTotal());
                ventana.setVisible(true);

                if (ventana.isConfirmado()) {
                    lblTotal.setText("Total: $" + String.format("%.2f", ventana.getTotalFinal()));
                } else {
                    // Restauramos la selección anterior: "Tarjeta"
                    cmbMetodoPago.setSelectedIndex(indiceSeleccionado);
                }
            }
        });


        // ----boton confirmar compra----

        JButton btnConfirmar = new JButton("Confirmar compra");
        btnConfirmar.setBackground(Color.decode("#D93025"));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnConfirmar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnConfirmar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnConfirmar.setBackground(Color.decode("#b3221c")); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btnConfirmar.setBackground(Color.decode("#D93025")); }
        });


        btnConfirmar.addActionListener(e -> {
            String metodo = (String) cmbMetodoPago.getSelectedItem();

            MediosDePago pago = "Tarjeta".equals(metodo)
                    ? new Tarjeta("123", "Banco Nación")
                    : new Transferencia(txtCBU.getText());

            Usuario usuario = controlador.getUsuarioActual();
            carrito.setUsuario(usuario instanceof Cliente
                    ? (Cliente) usuario
                    : new Cliente(usuario.getNombre(), usuario.getEdad(), usuario.getCorreoElectronico(), usuario.getContrasenia()));

            String idFactura = "F" + String.format("%03d", controlador.obtenerFacturas().size() + 1);
            Factura factura = new Factura(idFactura, carrito, pago);

            if (factura.procesarPago()) {
                String textoFactura = factura.generarTextoFactura();
                mostrarFacturaPanel(textoFactura);
                controlador.getGestorCompras().agregarFactura(factura);
                controlador.getGestorCompras().guardarCompras();


                for (Map.Entry<Producto, Integer> p : carrito.getProductos().entrySet()) {
                    controlador.getSistemaAlmacen().getGestorProductos().actualizarStockProducto(p.getKey(), p.getValue());
                }

                // --- ENVIAR FACTURA POR CORREO ---
                EnviadorEmail enviador = new EnviadorEmail();
                String correoCliente = controlador.getUsuarioActual().getCorreoElectronico();
                enviador.enviarFactura(correoCliente, textoFactura);

                controlador.obtenerFacturas().add(factura.toString()); // o mejor: controlador.guardarFactura(factura);
                JOptionPane.showMessageDialog(this, "Compra confirmada y factura generada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dispose();


            } else {
                JOptionPane.showMessageDialog(this, "Error al procesar el pago.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlInferior.add(btnConfirmar);

        pnlPrincipal.add(pnlInferior, BorderLayout.SOUTH);

        pnlInferior.add(Box.createVerticalStrut(18));

        //---- boton volver----

        JButton btnVolver= new JButton("Volver");
        btnVolver.setBackground(Color.decode("#D93025"));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnVolver.setFocusPainted(false);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlInferior.add(btnVolver);

        btnVolver.addActionListener(e->{
            this.dispose();
        });

    }

    private void cargarProductosCarrito() {
        modeloTabla.setRowCount(0);
        for (Map.Entry<Producto, Integer> entry : carrito.getProductos().entrySet()) {
            Producto p = entry.getKey();
            int cantidad = entry.getValue();
            double subtotal = p.getPrecioProducto() * cantidad;
            modeloTabla.addRow(new Object[]{
                    p.getNombreProducto(),
                    cantidad,
                    "$" + p.getPrecioProducto(),
                    "$" + subtotal
            });
        }
    }

    private double calcularTotal() {
        return carrito.getProductos().entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrecioProducto() * e.getValue())
                .sum();
    }

    private void mostrarFacturaPanel(String textoFactura) {
        JTextArea area = new JTextArea(textoFactura);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(area);

        JDialog dialog = new JDialog(this, "Factura generada", true);
        dialog.add(scroll);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
