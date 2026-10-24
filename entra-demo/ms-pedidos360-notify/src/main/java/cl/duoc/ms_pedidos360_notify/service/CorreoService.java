package cl.duoc.ms_pedidos360_notify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private final JavaMailSender mailSender;
    private final String remitente;

    public CorreoService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String remitente
    ) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void enviarCorreo(
            String destinatario,
            String asunto,
            String contenido
    ) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "El destinatario del correo es obligatorio"
            );
        }

        SimpleMailMessage correo = new SimpleMailMessage();
        correo.setFrom(remitente);
        correo.setTo(destinatario);
        correo.setSubject(asunto);
        correo.setText(contenido);

        mailSender.send(correo);

        System.out.println(
                "Correo enviado correctamente a: " + destinatario
        );
    }
}