package cl.duoc.ms_pedidos360_notify.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import cl.duoc.ms_pedidos360_notify.dto.PedidoCreadoEvent;

@Slf4j
@Service
public class NotificacionService {

    public void procesarPedidoCreado(PedidoCreadoEvent evento) {

        if (evento == null || evento.pedidoId() == null) {
            throw new IllegalArgumentException(
                    "El evento debe contener el ID del pedido"
            );
        }

        log.info(
                "Procesando notificación. Tipo: {}, Pedido: {}, Estado: {}",
                evento.tipo(),
                evento.pedidoId(),
                evento.estado()
        );

        // Aquí incorporaremos el envío real de la notificación.
    }
}