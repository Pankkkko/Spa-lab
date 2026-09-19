package cl.duoc.msmascotas.dto;

public record MascotaResponse(
    Long id,
    String nombre,
    String tipo,
    Integer edad
) {
}