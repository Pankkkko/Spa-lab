package cl.duoc.ms_pedidos360_report.dto;

import java.time.Instant;

import tools.jackson.databind.JsonNode;

public record PedidoKafkaEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        String traceId,
        String correlationId,
        JsonNode payload
) {
}