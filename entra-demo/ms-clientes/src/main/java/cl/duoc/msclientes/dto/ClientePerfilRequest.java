package cl.duoc.msclientes.dto;

public record ClientePerfilRequest(
    String entraObjectId,
    String nombre,
    String email,
    String telefono
) {
}