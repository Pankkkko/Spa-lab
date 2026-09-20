package cl.duoc.mspedidos.dto;

import java.time.LocalDate;
import java.util.List;

public record PedidoResponse(
    Long id,
    Long clienteId,
    String clienteNombre,
    String estado,
    LocalDate fecha,
    Double total,
    List<PedidoDetalleResponse> detalles
) { }