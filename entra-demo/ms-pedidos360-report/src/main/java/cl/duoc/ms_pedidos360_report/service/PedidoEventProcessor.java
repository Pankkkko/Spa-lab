package cl.duoc.ms_pedidos360_report.service;

import cl.duoc.ms_pedidos360_report.dto.PedidoKafkaEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class PedidoEventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoEventProcessor.class);

    public void procesar(PedidoKafkaEvent evento) {

        log.info(
                "Procesando evento de pedido | EventId: {} | Tipo: {}",
                evento.eventId(),
                evento.eventType()
        );

        /*
         * Próxima implementación:
         *
         * 1. Comprobar si el evento ya fue procesado.
         * 2. Registrar el evento recibido.
         * 3. Actualizar el estado del pedido.
         * 4. Actualizar estadísticas de ventas.
         * 5. Actualizar estadísticas por producto.
         */
    }
}