
package cl.duoc.ms_pedidos360_audit.service;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ms_pedidos360_audit.dto.AuditTimelineEvent;
import cl.duoc.ms_pedidos360_audit.dto.PedidoKafkaEvent;
import cl.duoc.ms_pedidos360_audit.entity.RegistroAuditoria;
import cl.duoc.ms_pedidos360_audit.messaging.AuditKafkaProducer;
import cl.duoc.ms_pedidos360_audit.repository.RegistroAuditoriaRepository;
import tools.jackson.databind.JsonNode;

@Service
public class AuditEventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(AuditEventProcessor.class);

    private static final Map<String, String> EVENTOS_ESTADO = Map.of(
            "CREADO", "OrderCreated",
            "ACEPTADO", "OrderAccepted",
            "EN_PREPARACION", "OrderPreparing",
            "DESPACHADO", "OrderDispatched",
            "ENTREGADO", "OrderDelivered",
            "CANCELADO", "OrderCancelled"
    );

    private final RegistroAuditoriaRepository auditoriaRepository;
    private final AuditKafkaProducer kafkaProducer;

    public AuditEventProcessor(
            RegistroAuditoriaRepository auditoriaRepository,
            AuditKafkaProducer kafkaProducer) {

        this.auditoriaRepository = auditoriaRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public void procesar(PedidoKafkaEvent evento) {

        // 1. Evitar procesar eventos duplicados.
        if (auditoriaRepository.existsByEventId(evento.eventId())) {

            log.info(
                    "Evento duplicado ignorado: {}",
                    evento.eventId()
            );

            return;
        }

        JsonNode payload = evento.payload();

        // 2. Obtener identificador del pedido.
        JsonNode pedidoNodo = payload.path("pedidoId");

        if (pedidoNodo.isMissingNode() || pedidoNodo.isNull()
                || pedidoNodo.asText().isBlank()) {

            throw new IllegalArgumentException(
                    "El evento no contiene un pedidoId válido"
            );
        }

        String pedidoId = pedidoNodo.asText();

        // 3. Obtener el identificador del cliente.
        Long clienteId = obtenerLong(payload, "clienteId");

        // 4. Obtener y normalizar el estado.
        String estado = obtenerTexto(payload, "estado");

        if (estado == null) {

            throw new IllegalArgumentException(
                    "El evento no contiene un estado"
            );
        }

        String tipoEvento = EVENTOS_ESTADO.get(
                estado.toUpperCase()
        );

        if (tipoEvento == null) {

            throw new IllegalArgumentException(
                    "Estado de pedido desconocido: " + estado
            );
        }

        Instant fechaEvento = evento.timestamp();
        Instant fechaRecepcion = Instant.now();

        // 5. Obtener información adicional.
        String descripcion = "El pedido #"
                + pedidoId
                + " cambió al estado "
                + estado.toUpperCase();

        // El contrato actual no proporciona actor ni origen.
        String usuarioId = null;
        String usuarioNombre = null;
        String origen = null;

        // 6. Crear registro de auditoría.
        RegistroAuditoria registro = new RegistroAuditoria();

        registro.setEventId(evento.eventId());
        registro.setUsuarioId(usuarioId);
        registro.setUsuarioNombre(usuarioNombre);
        registro.setClienteId(clienteId);
        registro.setTipoEvento(tipoEvento);
        registro.setEntidad("PEDIDO");
        registro.setEntidadId(pedidoId);
        registro.setDescripcion(descripcion);
        registro.setFechaEvento(fechaEvento);
        registro.setOrigen(origen);
        registro.setTraceId(evento.traceId());
        registro.setCorrelationId(evento.correlationId());
        registro.setPayload(payload.toString());
        registro.setFechaRecepcion(fechaRecepcion);

        // 7. Persistir el registro.
        auditoriaRepository.saveAndFlush(registro);

        // 8. Construir el evento normalizado.
        AuditTimelineEvent eventoAuditoria = new AuditTimelineEvent(
                evento.eventId(),
                usuarioId,
                usuarioNombre,
                tipoEvento,
                "PEDIDO",
                pedidoId,
                descripcion,
                fechaEvento,
                origen,
                evento.traceId(),
                evento.correlationId(),
                payload.toString()
        );

        // 9. Publicar en audit.timeline.
        kafkaProducer.publicar(eventoAuditoria).join();

        log.info(
                "Evento auditado correctamente | Pedido: {} | Cliente: {} | Tipo: {}",
                pedidoId,
                clienteId,
                tipoEvento
        );
    }

    private String obtenerTexto(JsonNode nodo, String campo) {

        JsonNode valor = nodo.path(campo);

        if (valor.isMissingNode() || valor.isNull()) {
            return null;
        }

        String texto = valor.asText();

        return texto.isBlank() ? null : texto;
    }

    private Long obtenerLong(JsonNode nodo, String campo) {

        JsonNode valor = nodo.path(campo);

        if (valor.isMissingNode() || valor.isNull()) {
            return null;
        }

        String texto = valor.asText();

        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " no contiene un identificador válido: " + texto,
                    e
            );
        }
    }
}
