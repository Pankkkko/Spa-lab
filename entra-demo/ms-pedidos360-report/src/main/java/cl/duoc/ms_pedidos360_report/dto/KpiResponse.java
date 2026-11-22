package cl.duoc.ms_pedidos360_report.dto;

import java.math.BigDecimal;

public record KpiResponse(
        long totalPedidos,
        long pedidosActivos,
        long pedidosEntregados,
        long pedidosCancelados,
        BigDecimal ventasTotales,
        double tiempoPromedioEntregaMinutos
) {}