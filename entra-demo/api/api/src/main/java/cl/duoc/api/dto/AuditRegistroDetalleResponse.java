package cl.duoc.api.dto;

import java.time.Instant;

public record AuditRegistroDetalleResponse(
    Long id,
    String eventId,
    String usuarioId,
    String usuarioNombre,
    Long clienteId,
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