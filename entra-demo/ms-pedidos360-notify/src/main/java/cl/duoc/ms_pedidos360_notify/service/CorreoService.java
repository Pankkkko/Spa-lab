package cl.duoc.ms_pedidos360_notify.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

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

    // Se conserva para no romper los consumidores actuales.
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

    // Se conserva el envío de facturas y otros adjuntos.
    public void enviarCorreoConAdjunto(
            String destinatario,
            String asunto,
            String contenido,
            String nombreAdjunto,
            byte[] archivoAdjunto
    ) {
        enviarMensaje(
                destinatario,
                asunto,
                contenido,
                false,
                nombreAdjunto,
                archivoAdjunto
        );
    }

    // NUEVO: correo para cambios de estado.
    public void enviarCorreoCambioEstado(
            String destinatario,
            Long pedidoId,
            String nuevoEstado
    ) {
        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "El ID del pedido es obligatorio"
            );
        }

        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException(
                    "El nuevo estado es obligatorio"
            );
        }

        String estado = nuevoEstado
                .trim()
                .toUpperCase(Locale.ROOT);

        String titulo;
        String descripcion;
        String emoji;

        switch (estado) {
            case "CREADO" -> {
                titulo = "¡Recibimos tu pedido!";
                descripcion = "Tu pedido fue registrado correctamente.";
                emoji = "📝";
            }
            case "ACEPTADO" -> {
                titulo = "¡Tu pedido fue aceptado!";
                descripcion = "Hemos aceptado tu pedido y pronto comenzaremos a prepararlo.";
                emoji = "✅";
            }
            case "EN_PREPARACION" -> {
                titulo = "¡Estamos preparando tu pedido!";
                descripcion = "Nuestro equipo está trabajando para dejar tu pedido listo.";
                emoji = "📦";
            }
            case "DESPACHADO" -> {
                titulo = "¡Tu pedido está en camino!";
                descripcion = "Tu pedido fue despachado y va rumbo a su destino.";
                emoji = "🚚";
            }
            case "ENTREGADO" -> {
                titulo = "¡Tu pedido fue entregado!";
                descripcion = "Tu pedido ha sido marcado como entregado. ¡Gracias por preferirnos!";
                emoji = "🎉";
            }
            case "CANCELADO" -> {
                titulo = "Tu pedido fue cancelado";
                descripcion = "El pedido fue cancelado. Si tienes dudas, puedes comunicarte con nuestro equipo.";
                emoji = "ℹ️";
            }
            default -> {
                titulo = "Actualización de tu pedido";
                descripcion = "El estado de tu pedido ha sido actualizado.";
                emoji = "🔔";
            }
        }

        String estadoVisible = escaparHtml(
                estado.replace("_", " ")
        );

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">
            </head>
            <body style="
                margin:0;
                padding:30px 12px;
                background-color:#eef8f6;
                font-family:Arial,Helvetica,sans-serif;
                color:#0C3B45;
            ">

                <table role="presentation"
                       cellpadding="0"
                       cellspacing="0"
                       style="
                           width:100%%;
                           max-width:560px;
                           margin:0 auto;
                           background:#ffffff;
                           border-radius:16px;
                       ">

                    <tr>
                        <td style="
                            background:#20666B;
                            padding:28px;
                            text-align:center;
                            border-radius:16px 16px 0 0;
                        ">
                            <h1 style="
                                color:#ffffff;
                                margin:0;
                                font-size:26px;
                            ">
                                Pedidos360
                            </h1>

                            <p style="
                                color:#CFF0EA;
                                margin:8px 0 0;
                                font-size:14px;
                            ">
                                Notificaciones de tu compra
                            </p>
                        </td>
                    </tr>

                    <tr>
                        <td style="
                            padding:35px 30px;
                            text-align:center;
                        ">

                            <div style="font-size:48px;">
                                %s
                            </div>

                            <h2 style="
                                font-size:23px;
                                margin:18px 0 12px;
                                color:#20666B;
                            ">
                                %s
                            </h2>

                            <p style="
                                font-size:15px;
                                line-height:1.7;
                                color:#52666A;
                            ">
                                %s
                            </p>

                            <table role="presentation"
                                   style="
                                       width:100%%;
                                       background:#F0FAF8;
                                       border-radius:12px;
                                       margin:25px 0;
                                       padding:20px;
                                   ">
                                <tr>
                                    <td style="
                                        text-align:left;
                                        font-size:13px;
                                        color:#52666A;
                                    ">
                                        Número de pedido
                                    </td>

                                    <td style="
                                        text-align:right;
                                        font-weight:bold;
                                    ">
                                        #%d
                                    </td>
                                </tr>

                                <tr>
                                    <td colspan="2"
                                        style="height:14px;">
                                    </td>
                                </tr>

                                <tr>
                                    <td style="
                                        text-align:left;
                                        font-size:13px;
                                        color:#52666A;
                                    ">
                                        Estado actual
                                    </td>

                                    <td style="
                                        text-align:right;
                                        font-weight:bold;
                                        color:#3E9B94;
                                    ">
                                        %s
                                    </td>
                                </tr>
                            </table>

                            <p style="
                                font-size:13px;
                                line-height:1.6;
                                color:#74878A;
                            ">
                                Este mensaje fue generado
                                automáticamente por Pedidos360.
                            </p>

                        </td>
                    </tr>

                    <tr>
                        <td style="
                            background:#CFF0EA;
                            text-align:center;
                            padding:18px;
                            border-radius:0 0 16px 16px;
                            font-size:12px;
                            color:#20666B;
                        ">
                            © Pedidos360 · Sistema de notificaciones
                        </td>
                    </tr>

                </table>

            </body>
            </html>
            """.formatted(
                emoji,
                titulo,
                descripcion,
                pedidoId,
                estadoVisible
        );

        enviarMensaje(
                destinatario,
                "Pedidos360 | Actualización del pedido #" + pedidoId,
                html,
                true,
                null,
                null
        );
    }

    private String escaparHtml(String valor) {
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Método interno compartido.
    private void enviarMensaje(
            String destinatario,
            String asunto,
            String contenido,
            boolean esHtml,
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

            // Ahora admite texto plano y HTML.
            helper.setText(contenido, esHtml);

            if (archivoAdjunto != null && archivoAdjunto.length > 0) {
                if (nombreAdjunto == null || nombreAdjunto.isBlank()) {
                    throw new IllegalArgumentException(
                            "El nombre del adjunto es obligatorio"
                    );
                }

                helper.addAttachment(
                        nombreAdjunto,
                        new ByteArrayResource(archivoAdjunto),
                        "application/pdf"
                );
            }

            mailSender.send(mensaje);

            System.out.println("Correo enviado correctamente.");

        } catch (MessagingException ex) {
            throw new IllegalStateException(
                    "No fue posible preparar el correo electrónico",
                    ex
            );
        }
    }
}