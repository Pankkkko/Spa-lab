
package cl.duoc.ms_pedidos360_report.dto;

public record LeadTimeResponse(
    Long completedOrders,
    Double averageMinutes,
    Double minimumMinutes,
    Double maximumMinutes
) {
}
