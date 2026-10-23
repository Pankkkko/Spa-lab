
package cl.duoc.mspedidos.dto;

import java.time.Instant;

import tools.jackson.databind.JsonNode;

public record ComandoEnvelope(
        String type,
        String eventId,
        Instant timestamp,
        String traceId,
        String correlationId,
        JsonNode payload
) {}