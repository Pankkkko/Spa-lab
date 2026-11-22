
package cl.duoc.ms_pedidos360_audit.dto;

import java.time.Instant;

public record AuditRegistroDetalleResponse(
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
    String correlationId,
    String payload,
    Instant fechaRecepcion
) {}
