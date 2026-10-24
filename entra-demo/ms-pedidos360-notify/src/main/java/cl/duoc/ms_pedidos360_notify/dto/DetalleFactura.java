package cl.duoc.ms_pedidos360_notify.dto;

public record DetalleFactura(
        String producto,
        int cantidad,
        double precioUnitario,
        double subtotal
) {
}