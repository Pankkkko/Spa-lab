package cl.duoc.api.dto;

public record PedidoDetalleResponse(
    String producto,
    Integer cantidad,
    Double precioUnitario,
    Double subtotal
) { }