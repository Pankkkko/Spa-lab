
package cl.duoc.ms_pedidos360_report.dto;

import java.math.BigDecimal;

public record KpiResponse(
    Long totalOrders,
    Long activeOrders,
    Long deliveredOrders,
    Long cancelledOrders,
    BigDecimal totalSales,
    Double averageLeadTimeMinutes
) {
}
