
package cl.duoc.ms_pedidos360_notify.service;

import cl.duoc.ms_pedidos360_notify.dto.ComandoEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificacionService {

    public void procesarEmail(ComandoEnvelope evento) {
        validarEvento(evento);

        log.info(
                "Procesando comando de correo. Tipo: {}, eventId: {}, correlationId: {}",
                evento.type(),
                evento.eventId(),
                evento.correlationId()
        );

        // TODO: integrar un proveedor de correo.
    }

    public void procesarPedido(ComandoEnvelope evento) {
        validarEvento(evento);

        log.info(
                "Procesando comando de pedido. Tipo: {}, eventId: {}, correlationId: {}",
                evento.type(),
                evento.eventId(),
                evento.correlationId()
        );

        // TODO: incorporar aquí el procesamiento que corresponda
        // al flujo de pedidos.
    }

    public void procesarFactura(ComandoEnvelope evento) {
        validarEvento(evento);

        log.info(
                "Procesando comando de factura. Tipo: {}, eventId: {}, correlationId: {}",
                evento.type(),
                evento.eventId(),
                evento.correlationId()
        );

        // TODO: integrar la generación de boletas o facturas.
    }

    private void validarEvento(ComandoEnvelope evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }

        if (evento.type() == null || evento.type().isBlank()) {
            throw new IllegalArgumentException("El tipo de evento es obligatorio");
        }

        if (evento.eventId() == null || evento.eventId().isBlank()) {
            throw new IllegalArgumentException("eventId es obligatorio");
        }

        if (evento.timestamp() == null) {
            throw new IllegalArgumentException("timestamp es obligatorio");
        }

        if (evento.payload() == null || evento.payload().isNull()) {
            throw new IllegalArgumentException("payload es obligatorio");
        }
    }
}
