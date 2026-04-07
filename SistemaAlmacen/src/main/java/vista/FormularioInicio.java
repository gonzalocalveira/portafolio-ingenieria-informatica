package vista;

import controlador.ControladorSistemaAlmacen;
import modelo.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class FormularioInicio extends JFrame {

    private JPanel pnlPrincipal;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JButton btnAgregarAlCarrito;
    private JButton btnVerCarrito;
    private JSpinner spnCantidad;
    private CarritoDeCompra carrito;
    private ControladorSistemaAlmacen sistemaAlmacen;
    private JButton btnCerrarSesion;
    private JButton ayuda;

    public FormularioInicio(ControladorSistemaAlmacen controlador, Usuario usuarioActual, CarritoDeCompra carritoCompartido) {
        this.sistemaAlmacen = controlador;

        if (carritoCompartido != null) {
            this.carrito = carritoCompartido;
        } else {

            // Asocia el carrito al usuario logueado
            this.carrito = new CarritoDeCompra(
                    usuarioActual instanceof Cliente
                            ? (Cliente) usuarioActual
                            : new Cliente(
                            usuarioActual.getNombre(),
                            usuarioActual.getEdad(),
                            usuarioActual.getCorreoElectronico(),
                            usuarioActual.getContrasenia()
                    )
            );
        }

        setTitle("Sistema Almacén de Supermercado");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        iniciarComponentes();
        setVisible(true);
    }

    private void iniciarComponentes() {
        pnlPrincipal = new JPanel(new BorderLayout(10, 10));
        pnlPrincipal.setBackground(Color.decode("#fff5f5"));
        add(pnlPrincipal);

        pnlPrincipal = new JPanel(new BorderLayout(10, 10));
        pnlPrincipal.setBackground(Color.decode("#fff5f5"));
        add(pnlPrincipal);

// PANEL SUPERIOR PARA TITULO + AYUDA
        JPanel pnlSuperior = new JPanel(new BorderLayout());
        pnlSuperior.setBackground(Color.decode("#fff5f5"));

        // TÍTULO
        JLabel lblTitulo = new JLabel("Productos Disponibles", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.decode("#D93025"));
        pnlSuperior.add(lblTitulo, BorderLayout.CENTER);

        // BOTÓN DE AYUDA
        ayuda = new JButton("?");
        ayuda.setPreferredSize(new Dimension(45, 35));
        ayuda.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ayuda.setBackground(Color.decode("#D93025"));
        ayuda.setForeground(Color.WHITE);
        ayuda.setFocusPainted(false);

// Panel para alinearlo a la derecha
        JPanel pnlDerecha = new JPanel();
        pnlDerecha.setBackground(Color.decode("#fff5f5"));
        pnlDerecha.add(ayuda);

        pnlSuperior.add(pnlDerecha, BorderLayout.EAST);

// Agregar al panel principal
        pnlPrincipal.add(pnlSuperior, BorderLayout.NORTH);


        modeloTabla = new DefaultTableModel(new Object[]{"Nombre", "Precio", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setRowHeight(30);
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
            tablaProductos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = tablaProductos.getTableHeader();
        header.setBackground(Color.decode("#D93025"));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        scrollPane.getViewport().setBackground(Color.decode("#fff5f5"));
        pnlPrincipal.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlInferior = new JPanel();
        pnlInferior.setBackground(Color.decode("#fff5f5"));
        pnlInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCantidad.setForeground(Color.decode("#D93025"));
        pnlInferior.add(lblCantidad);

        spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spnCantidad.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnlInferior.add(spnCantidad);

        btnAgregarAlCarrito = new JButton("Agregar al carrito");
        btnAgregarAlCarrito.setBackground(Color.decode("#D93025"));
        btnAgregarAlCarrito.setForeground(Color.WHITE);
        btnAgregarAlCarrito.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAgregarAlCarrito.setFocusPainted(false);
        pnlInferior.add(btnAgregarAlCarrito);

        btnVerCarrito = new JButton("Ver carrito");
        btnVerCarrito.setBackground(Color.decode("#D93025"));
        btnVerCarrito.setForeground(Color.WHITE);
        btnVerCarrito.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVerCarrito.setFocusPainted(false);
        pnlInferior.add(btnVerCarrito);

        btnCerrarSesion = new JButton("Cerrar Sesion");
        btnCerrarSesion.setBackground(Color.decode("#D93025"));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrarSesion.setFocusPainted(false);
        pnlInferior.add(btnCerrarSesion);


        pnlPrincipal.add(pnlInferior, BorderLayout.SOUTH);

        cargarProductosDesdeSistema();

        // Listener para agregar productos al carrito
        btnAgregarAlCarrito.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila != -1) {
                Producto seleccionado = sistemaAlmacen.obtenerProductos().get(fila);
                int cantidad = (int) spnCantidad.getValue();

                if (cantidad > seleccionado.getStock()) {
                    JOptionPane.showMessageDialog(this, "No hay suficiente stock", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                carrito.agregarProducto(seleccionado, cantidad);
                modeloTabla.setValueAt(seleccionado.getStock() - cantidad, fila, 2);
                seleccionado.setStock(seleccionado.getStock() - cantidad);
                JOptionPane.showMessageDialog(this, "Producto agregado al carrito");
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Listener para ver carrito
        btnVerCarrito.addActionListener(e -> {
            new FormularioCarrito(carrito, sistemaAlmacen);
        });

        //Listener para cerrar sesion
        btnCerrarSesion.addActionListener(e -> {
            new FormularioInicioSesionUsuario().setVisible(true);
            this.dispose();
        });

        //Listener para boton ayuda
        ayuda.addActionListener(e -> {
            new FormularioAyuda().setVisible(true);
        });

    }

    private void cargarProductosDesdeSistema() {
        List<Producto> productos = sistemaAlmacen.obtenerProductos();
        for (Producto p : productos) {
            modeloTabla.addRow(new Object[]{p.getNombreProducto(), "$" + p.getPrecioProducto(), p.getStock()});
        }
    }
}
