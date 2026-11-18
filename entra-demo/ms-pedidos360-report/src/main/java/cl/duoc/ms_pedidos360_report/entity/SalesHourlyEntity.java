
package cl.duoc.ms_pedidos360_report.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "sales_hourly",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_sales_hour",
            columnNames = {"hour_start"}
        )
    }
)
public class SalesHourlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hour_start", nullable = false)
    private LocalDateTime hourStart;

    @Column(name = "order_count", nullable = false)
    private Long orderCount = 0L;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public SalesHourlyEntity() {
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getHourStart() {
        return hourStart;
    }

    public void setHourStart(LocalDateTime hourStart) {
        this.hourStart = hourStart;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
