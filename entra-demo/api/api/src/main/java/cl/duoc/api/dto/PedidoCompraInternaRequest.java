package cl.duoc.api.dto;

import java.util.List;

public record PedidoCompraInternaRequest(
    Long clienteId,
    List<PedidoDetalleCompraInternaRequest> detalles
) {}