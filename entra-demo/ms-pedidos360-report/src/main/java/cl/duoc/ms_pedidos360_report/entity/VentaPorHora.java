package cl.duoc.ms_pedidos360_report.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "ventas_por_hora",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "hora_inicio")
    }
)
public class VentaPorHora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hora_inicio", nullable = false)
    private LocalDateTime horaInicio;

    @Column(nullable = false)
    private Long cantidadPedidos = 0L;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal = BigDecimal.ZERO;

    public VentaPorHora() {
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalDateTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Long getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(Long cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
}