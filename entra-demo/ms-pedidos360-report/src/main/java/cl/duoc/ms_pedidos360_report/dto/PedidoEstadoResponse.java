package cl.duoc.ms_pedidos360_report.dto;

import cl.duoc.ms_pedidos360_report.entity.EstadoPedido;

public record PedidoEstadoResponse(
        EstadoPedido estado,
        long cantidad
) {}