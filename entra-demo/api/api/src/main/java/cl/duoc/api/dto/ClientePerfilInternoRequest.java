package cl.duoc.api.dto;

public record ClientePerfilInternoRequest(
    String entraObjectId,
    String nombre,
    String email,
    String telefono
) {
}