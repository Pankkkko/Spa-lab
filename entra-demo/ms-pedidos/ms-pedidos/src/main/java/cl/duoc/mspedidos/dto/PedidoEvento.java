package cl.duoc.mspedidos.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoEvento(
    String eventId,
    String type,
    Long pedidoId,
    String estado,
    LocalDateTime timestamp,
    String correlationId
) {
    public static PedidoEvento of(String type, Long pedidoId, String estado) {
        return new PedidoEvento(
            UUID.randomUUID().toString(),
            type,
            pedidoId,
            estado,
            LocalDateTime.now(),
            UUID.randomUUID().toString()
        );
    }
}