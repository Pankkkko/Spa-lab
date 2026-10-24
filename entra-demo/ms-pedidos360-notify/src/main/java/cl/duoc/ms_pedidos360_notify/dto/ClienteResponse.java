package cl.duoc.ms_pedidos360_notify.dto;

public record ClienteResponse(
        Long id,
        String nombre,
        String email,
        String telefono
) {
}