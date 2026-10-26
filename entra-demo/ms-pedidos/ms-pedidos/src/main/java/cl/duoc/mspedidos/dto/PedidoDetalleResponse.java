
package cl.duoc.mspedidos.dto;

import java.math.BigDecimal;

public record PedidoDetalleResponse(
    String producto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}
