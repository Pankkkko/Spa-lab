
package cl.duoc.ms_pedidos360_notify.service;

import cl.duoc.ms_pedidos360_notify.dto.ClienteResponse;
import cl.duoc.ms_pedidos360_notify.dto.ComandoEnvelope;
import cl.duoc.ms_pedidos360_notify.dto.DetalleFactura;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class NotificacionService {

    private static final long CLIENTE_ID_PRUEBA = 1L;
    private static final BigDecimal TASA_IVA = new BigDecimal("0.19");

    private final CorreoService correoService;
    private final FacturaPdfService facturaPdfService;
    private final RestClient restClient;

    public NotificacionService(
            CorreoService correoService,
            FacturaPdfService facturaPdfService,
            RestClient.Builder restClientBuilder,
            @Value("${clientes.base-url}") String clientesBaseUrl
    ) {
        this.correoService = correoService;
        this.facturaPdfService = facturaPdfService;

        this.restClient = restClientBuilder
                .baseUrl(clientesBaseUrl)
                .build();
    }

    public void procesarEmail(ComandoEnvelope evento) {
        validarEvento(evento);

        log.info(
                "Procesando comando de correo. Tipo: {}, eventId: {}, correlationId: {}",
                evento.type(),
                evento.eventId(),
                evento.correlationId()
        );

        var payload = evento.payload();

        long clienteId = payload.path("clienteId")
                .asLong(CLIENTE_ID_PRUEBA);

        ClienteResponse cliente = restClient.get()
                .uri("/api/clientes/{id}", clienteId)
                .retrieve()
                .body(ClienteResponse.class);

        if (cliente == null) {
            throw new IllegalStateException(
                    "El microservicio de clientes no devolvió información"
            );
        }

        if (cliente.email() == null || cliente.email().isBlank()) {
            throw new IllegalStateException(
                    "El cliente no tiene un email registrado"
            );
        }

        long pedidoId = payload.path("pedidoId").asLong();
        String estado = payload.path("estado").asText("SIN ESTADO");
        String fecha = payload.path("fecha").asText("SIN FECHA");

        // Los precios del catálogo son netos, sin IVA.
        BigDecimal subtotalNeto = BigDecimal.valueOf(
                payload.path("total").asDouble()
        ).setScale(0, RoundingMode.HALF_UP);

        // Calcular el IVA y el total final en pesos chilenos.
        BigDecimal iva = subtotalNeto
                .multiply(TASA_IVA)
                .setScale(0, RoundingMode.HALF_UP);

        BigDecimal totalConIva = subtotalNeto.add(iva);

        List<DetalleFactura> detalles = new ArrayList<>();
        var detallesJson = payload.path("detalles");

        if (detallesJson.isArray()) {
            detallesJson.forEach(detalle -> {
                detalles.add(new DetalleFactura(
                        detalle.path("producto")
                                .asText("Producto sin nombre"),
                        detalle.path("cantidad").asInt(1),
                        detalle.path("precioUnitario").asDouble(),
                        detalle.path("subtotal").asDouble()
                ));
            });
        }

        NumberFormat formatoMoneda =
                NumberFormat.getCurrencyInstance(
                        Locale.forLanguageTag("es-CL")
                );

        String asunto = "Confirmación de pedido #" + pedidoId;
        StringBuilder contenido = new StringBuilder();

        contenido.append("Hola ")
                .append(
                        cliente.nombre() != null
                                ? cliente.nombre()
                                : "cliente"
                )
                .append(":\n\n");

        contenido.append(
                "Hemos registrado tu pedido en Pedidos360.\n\n"
        );

        contenido.append("RESUMEN DEL PEDIDO\n");
        contenido.append("------------------------------\n");

        contenido.append("Número de pedido: ")
                .append(pedidoId)
                .append("\n");

        contenido.append("Estado: ")
                .append(estado)
                .append("\n");

        contenido.append("Fecha: ")
                .append(fecha)
                .append("\n\n");

        contenido.append("DETALLE DE PRODUCTOS (PRECIOS NETOS)\n");
        contenido.append("------------------------------\n");

        if (detalles.isEmpty()) {
            contenido.append(
                    "No hay detalles de productos registrados.\n\n"
            );
        } else {
            for (DetalleFactura detalle : detalles) {
                contenido.append("Producto: ")
                        .append(detalle.producto())
                        .append("\n");

                contenido.append("Cantidad: ")
                        .append(detalle.cantidad())
                        .append("\n");

                contenido.append("Precio unitario neto: ")
                        .append(formatoMoneda.format(
                                detalle.precioUnitario()
                        ))
                        .append("\n");

                contenido.append("Subtotal neto: ")
                        .append(formatoMoneda.format(
                                detalle.subtotal()
                        ))
                        .append("\n\n");
            }
        }

        contenido.append("RESUMEN DE PAGO\n");
        contenido.append("------------------------------\n");

        contenido.append("Subtotal neto: ")
                .append(formatoMoneda.format(subtotalNeto))
                .append("\n");

        contenido.append("IVA (19%): ")
                .append(formatoMoneda.format(iva))
                .append("\n");

        contenido.append("TOTAL CON IVA: ")
                .append(formatoMoneda.format(totalConIva))
                .append("\n\n");

        contenido.append(
                "Adjuntamos el documento PDF de demostración "
                + "correspondiente a tu pedido.\n\n"
        );

        contenido.append(
                "Importante: el documento adjunto es ficticio "
                + "y no tiene validez tributaria.\n\n"
        );

        contenido.append(
                "Gracias por comprar con nosotros.\n\n"
                + "Equipo Pedidos360"
        );

        // Generar el PDF en memoria, incluyendo el IVA.
        byte[] facturaPdf = facturaPdfService.generarFactura(
                pedidoId,
                cliente.nombre(),
                cliente.email(),
                estado,
                fecha,
                subtotalNeto.doubleValue(),
                iva.doubleValue(),
                totalConIva.doubleValue(),
                detalles
        );

        String nombreAdjunto =
                "Factura_Demo_Pedido_" + pedidoId + ".pdf";

        // Un solo correo con el resumen y la factura adjunta.
        correoService.enviarCorreoConAdjunto(
                cliente.email(),
                asunto,
                contenido.toString(),
                nombreAdjunto,
                facturaPdf
        );

        log.info(
                "Correo de confirmación y PDF enviados. pedidoId: {}, eventId: {}",
                pedidoId,
                evento.eventId()
        );
    }

    public void procesarPedido(ComandoEnvelope evento) {
        validarEvento(evento);

        log.info(
                "Procesando comando de pedido. Tipo: {}, eventId: {}, correlationId: {}",
                evento.type(),
                evento.eventId(),
                evento.correlationId()
        );

        // Se conserva el procesamiento actual de pedidos.
    }

    public void procesarFactura(ComandoEnvelope evento) {
        validarEvento(evento);

        log.info(
                "Procesando comando de factura. Tipo: {}, eventId: {}, correlationId: {}",
                evento.type(),
                evento.eventId(),
                evento.correlationId()
        );

        /*
         * El PDF de demostración se genera durante EmailSolicitud
         * para incluirlo en el mismo correo.
         *
         * Este comando queda reservado para el futuro flujo
         * independiente de facturación.
         */
    }

    private void validarEvento(ComandoEnvelope evento) {
        if (evento == null) {
            throw new IllegalArgumentException(
                    "El evento no puede ser nulo"
            );
        }

        if (evento.type() == null || evento.type().isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de evento es obligatorio"
            );
        }

        if (evento.eventId() == null || evento.eventId().isBlank()) {
            throw new IllegalArgumentException(
                    "eventId es obligatorio"
            );
        }

        if (evento.timestamp() == null) {
            throw new IllegalArgumentException(
                    "timestamp es obligatorio"
            );
        }

        if (evento.payload() == null || evento.payload().isNull()) {
            throw new IllegalArgumentException(
                    "payload es obligatorio"
            );
        }
    }
}
