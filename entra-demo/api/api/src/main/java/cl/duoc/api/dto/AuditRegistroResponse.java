
package cl.duoc.api.dto;

import java.time.Instant;

public record AuditRegistroResponse(
    Long id,
    String eventId,
    String usuarioId,
    String usuarioNombre,
    String tipoEvento,
    String entidad,
    String entidadId,
    String descripcion,
    Instant fechaEvento,
    String origen,
    String traceId,
    String correlationId
) {}
