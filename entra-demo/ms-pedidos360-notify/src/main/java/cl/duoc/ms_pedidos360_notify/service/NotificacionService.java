
package cl.duoc.ms_pedidos360_notify.service;

import cl.duoc.ms_pedidos360_notify.dto.ClienteResponse;
import cl.duoc.ms_pedidos360_notify.dto.ComandoEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
public class NotificacionService {

    private static final long CLIENTE_ID_PRUEBA = 1L;

    private final CorreoService correoService;
    private final RestClient restClient;

    public NotificacionService(
            CorreoService correoService,
            RestClient.Builder restClientBuilder,
            @Value("${clientes.base-url}") String clientesBaseUrl
    ) {
        this.correoService = correoService;
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

        // Cliente fijo para la evaluación actual.
        ClienteResponse cliente = restClient.get()
                .uri("/api/clientes/{id}", CLIENTE_ID_PRUEBA)
                .retrieve()
                .body(ClienteResponse.class);

        if (cliente == null) {
            throw new IllegalStateException(
                    "El microservicio de clientes no devolvió información"
            );
        }

        if (cliente.email() == null || cliente.email().isBlank()) {
            throw new IllegalStateException(
                    "El cliente " + CLIENTE_ID_PRUEBA
                            + " no tiene un email registrado"
            );
        }

        var payload = evento.payload();

        long pedidoId = payload.path("pedidoId").asLong();
        String estado = payload.path("estado").asText("SIN ESTADO");
        String fecha = payload.path("fecha").asText("SIN FECHA");
        double total = payload.path("total").asDouble();

        NumberFormat formatoMoneda =
                NumberFormat.getCurrencyInstance(
                        Locale.forLanguageTag("es-CL")
                );

        String asunto = "Confirmación de pedido #" + pedidoId;

        String contenido = """
                Hola %s:

                Hemos registrado tu pedido en Pedidos360.

                Número de pedido: %d
                Estado: %s
                Fecha: %s
                Total: %s

                Gracias por comprar con nosotros.

                Equipo Pedidos360
                """.formatted(
                cliente.nombre() != null ? cliente.nombre() : "cliente",
                pedidoId,
                estado,
                fecha,
                formatoMoneda.format(total)
        );

        correoService.enviarCorreo(
                cliente.email(),
                asunto,
                contenido
        );

        log.info(
                "Correo de confirmación enviado. pedidoId: {}, eventId: {}",
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

        // Se conserva el procesamiento actual de facturas.
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
