
package cl.duoc.api.dto;

public record TiempoEntregaResponse(
    long pedidosEntregados,
    double promedioMinutos,
    double minimoMinutos,
    double maximoMinutos
) {}
