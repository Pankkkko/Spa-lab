package cl.duoc.ms_pedidos360_report.messaging;

import cl.duoc.ms_pedidos360_report.dto.PedidoKafkaEvent;
import cl.duoc.ms_pedidos360_report.service.PedidoEventProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoKafkaConsumer.class);

    private final PedidoEventProcessor eventProcessor;

    public PedidoKafkaConsumer(PedidoEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.orders}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumir(PedidoKafkaEvent evento) {

        if (!esEventoValido(evento)) {
            log.warn("Evento Kafka rechazado: contenido inválido");
            return;
        }

        log.info(
                "Evento recibido desde Kafka | EventId: {} | Tipo: {}",
                evento.eventId(),
                evento.eventType()
        );

        eventProcessor.procesar(evento);
    }

    private boolean esEventoValido(PedidoKafkaEvent evento) {

        return evento != null
                && evento.eventId() != null
                && !evento.eventId().isBlank()
                && evento.eventType() != null
                && !evento.eventType().isBlank()
                && evento.timestamp() != null
                && evento.payload() != null
                && !evento.payload().isNull();
    }
}