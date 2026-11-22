package cl.duoc.ms_pedidos360_report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaPorHoraResponse(
        LocalDateTime hora,
        long cantidadPedidos,
        BigDecimal montoTotal
) {}