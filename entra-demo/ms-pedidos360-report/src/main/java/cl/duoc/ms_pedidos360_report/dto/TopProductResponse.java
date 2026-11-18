
package cl.duoc.ms_pedidos360_report.dto;

import java.math.BigDecimal;

public record TopProductResponse(
    String productId,
    String productName,
    Long unitsSold,
    BigDecimal totalAmount
) {
}
