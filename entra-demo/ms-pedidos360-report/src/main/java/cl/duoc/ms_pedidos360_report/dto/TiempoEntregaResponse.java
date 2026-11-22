package cl.duoc.ms_pedidos360_report.dto;

public record TiempoEntregaResponse(
        long pedidosEntregados,
        double promedioMinutos,
        double minimoMinutos,
        double maximoMinutos
) {}