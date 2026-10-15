package cl.duoc.ms_pedidos360_notify.dto;


public record PedidoCreadoEvent(
        String tipo,
        Long pedidoId,
        String estado
) {
}