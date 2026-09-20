package cl.duoc.api.dto;

import java.util.List;

public record ClienteDetalleResponse(
    Long id,
    String nombre,
    String email,
    String telefono,
    List<PedidoResponse> pedidos
) { }