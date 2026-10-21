
package cl.duoc.mspedidos.dto;

import java.time.Instant;

public record ComandoEnvelope(
        String type,
        String eventId,
        Instant timestamp,
        String traceId,
        String correlationId,
        Object payload
) {
}
