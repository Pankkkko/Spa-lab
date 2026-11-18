package cl.duoc.ms_pedidos360_report.dto;

import java.time.Instant;

import tools.jackson.databind.JsonNode;

public record ComandoEnvelope(
        String type,
        String eventId,
        Instant timestamp,
        String traceId,
        String correlationId,
        JsonNode payload
) {
}