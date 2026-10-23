package cl.duoc.ms_pedidos360_notify.dto;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record ComandoEnvelope(
        String type,
        String eventId,
        Instant timestamp,
        String traceId,
        String correlationId,
        JsonNode payload
) {
}
