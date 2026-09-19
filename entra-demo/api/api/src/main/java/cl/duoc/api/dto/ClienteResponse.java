package cl.duoc.api.dto;

import java.util.List;

public record ClienteResponse(
    Long id,
    String nombre,
    String email,
    List<MascotaResponse> mascotas
) {
}