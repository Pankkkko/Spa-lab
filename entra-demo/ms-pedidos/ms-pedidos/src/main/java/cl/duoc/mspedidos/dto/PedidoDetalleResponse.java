package cl.duoc.mspedidos.dto;

public record PedidoDetalleResponse(
    String producto,
    Integer cantidad,
    Double precioUnitario,
    Double subtotal
) { }