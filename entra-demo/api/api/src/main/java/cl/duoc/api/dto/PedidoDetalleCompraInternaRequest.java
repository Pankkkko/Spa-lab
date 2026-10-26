package cl.duoc.api.dto;

import java.math.BigDecimal;

public record PedidoDetalleCompraInternaRequest(
    String producto,
    Integer cantidad,
    BigDecimal precioUnitario
) {}