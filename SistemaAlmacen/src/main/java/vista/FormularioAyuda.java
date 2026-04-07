package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class FormularioAyuda extends JFrame {

    private JTextArea areaChat;
    private JTextField txtMensaje;
    private Map<String, String> respuestas;

    public FormularioAyuda() {
        setTitle("Centro de Ayuda");
        setSize(520, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Fondo general
        getContentPane().setBackground(Color.decode("#f7f7f7"));
        setLayout(new BorderLayout());

        cargarRespuestas();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.decode("#D93025"));
        header.setPreferredSize(new Dimension(0, 60));

        JLabel lblTitulo = new JLabel("Centro de Ayuda", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFocusable(false);
        areaChat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        areaChat.setBackground(Color.WHITE);
        areaChat.setForeground(Color.decode("#333333"));
        areaChat.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(areaChat);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode("#D93025"), 2));
        add(scroll, BorderLayout.CENTER);

        JPanel pnlInferior = new JPanel(new BorderLayout(5, 5));
        pnlInferior.setBackground(Color.decode("#f7f7f7"));
        pnlInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Campo de texto redondeado visualmente (no real)
        txtMensaje = new JTextField();
        txtMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMensaje.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#D93025"), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        txtMensaje.setBackground(Color.WHITE);
        pnlInferior.add(txtMensaje, BorderLayout.CENTER);

        // Botón enviar más moderno
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setBackground(Color.decode("#D93025"));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        pnlInferior.add(btnEnviar, BorderLayout.EAST);

        add(pnlInferior, BorderLayout.SOUTH);

        btnEnviar.addActionListener(this::procesarMensaje);
        txtMensaje.addActionListener(this::procesarMensaje);

        mostrarPreguntasFrecuentes();
    }

    private void cargarRespuestas() {
        respuestas = new HashMap<>();

        respuestas.put("como agrego productos al carrito",
                "Seleccioná un producto, elegí la cantidad y presioná 'Agregar al carrito'.");

        respuestas.put("no puedo agregar",
                "Verificá que hayas seleccionado un producto y que haya stock disponible.");

        respuestas.put("como veo el carrito",
                "Presioná el botón 'Ver carrito'.");

        respuestas.put("como cierro sesion",
                "Presioná el botón 'Cerrar Sesión'.");

        respuestas.put("hola",
                "¡Hola! ¿En qué puedo ayudarte?");
    }

    private void procesarMensaje(ActionEvent e) {
        String mensaje = txtMensaje.getText().trim();
        if (mensaje.isEmpty()) return;

        areaChat.append("> Tú: " + mensaje + "\n");

        String respuesta = buscarRespuesta(mensaje.toLowerCase());
        areaChat.append("-> Bot: " + respuesta + "\n\n");

        txtMensaje.setText("");
    }

    private String buscarRespuesta(String mensaje) {
        for (String clave : respuestas.keySet()) {
            if (mensaje.contains(clave)) {
                return respuestas.get(clave);
            }
        }

        return "No tengo una respuesta para eso. Probá con otra pregunta.";
    }

    private void mostrarPreguntasFrecuentes() {
        areaChat.append("  *Preguntas Frecuentes*\n\n");
        areaChat.append("• ¿Cómo agrego productos al carrito?\n");
        areaChat.append("• ¿Cómo veo el carrito?\n");
        areaChat.append("• ¿Cómo cierro sesión?\n");
        areaChat.append("• No puedo agregar un producto.\n");
        areaChat.append("• Hola.\n\n");
    }
}
