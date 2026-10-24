package cl.duoc.ms_pedidos360_notify.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

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
        enviarCorreoConAdjunto(
                destinatario,
                asunto,
                contenido,
                null,
                null
        );
    }

    public void enviarCorreoConAdjunto(
            String destinatario,
            String asunto,
            String contenido,
            String nombreAdjunto,
            byte[] archivoAdjunto
    ) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "El destinatario del correo es obligatorio"
            );
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido, false);

            if (archivoAdjunto != null && archivoAdjunto.length > 0) {
                if (nombreAdjunto == null || nombreAdjunto.isBlank()) {
                    throw new IllegalArgumentException(
                            "El nombre del archivo adjunto es obligatorio"
                    );
                }

                helper.addAttachment(
                        nombreAdjunto,
                        new ByteArrayResource(archivoAdjunto),
                        "application/pdf"
                );
            }

            mailSender.send(mensaje);

            // No registrar el correo del destinatario en los logs.
            System.out.println("Correo enviado correctamente.");

        } catch (MessagingException ex) {
            throw new IllegalStateException(
                    "No fue posible preparar el correo electrónico",
                    ex
            );
        }
    }
}