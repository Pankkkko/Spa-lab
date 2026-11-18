
package cl.duoc.ms_pedidos360_report.dto;

import cl.duoc.ms_pedidos360_report.entity.EstadoPedido;

public record OrdersByStatusResponse(
    EstadoPedido status,
    Long orderCount
) {
}
