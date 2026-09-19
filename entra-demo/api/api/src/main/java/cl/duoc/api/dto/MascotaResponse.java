package cl.duoc.api.dto;

public record MascotaResponse(
    Long id,
    String nombre,
    String tipo,
    Integer edad
) {
}