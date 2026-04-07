package servicio;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EnviadorEmail {

    private final String remitente = "sistemaAlmacen25@gmail.com";
    private final String clave = "qocrkveidqenfubj";

    public void enviarFactura(String destinatario, String cuerpo) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(remitente, clave);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Factura de compra");

            message.setText(cuerpo);

            Transport.send(message);

            System.out.println("Factura enviada por correo.");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
