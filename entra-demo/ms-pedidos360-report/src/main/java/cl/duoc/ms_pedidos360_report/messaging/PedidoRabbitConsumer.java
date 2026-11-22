package cl.duoc.ms_pedidos360_report.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.duoc.ms_pedidos360_report.config.RabbitMQReportConfig;
import cl.duoc.ms_pedidos360_report.dto.ComandoEnvelope;
import cl.duoc.ms_pedidos360_report.dto.PedidoKafkaEvent;

@Component
public class PedidoRabbitConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoRabbitConsumer.class);

    private final PedidoKafkaProducer kafkaProducer;

    public PedidoRabbitConsumer(PedidoKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @RabbitListener(
            queues = RabbitMQReportConfig.QUEUE_REPORT_PEDIDOS
    )
    public void recibirCambioEstado(ComandoEnvelope evento) {

        if (!esEventoValido(evento)) {

            log.error(
                    "Mensaje RabbitMQ inválido. Se enviará a recuperación."
            );

            throw new IllegalArgumentException(
                    "Evento RabbitMQ inválido"
            );
        }

        log.info(
                "Evento recibido desde RabbitMQ | EventId: {} | Tipo: {}",
                evento.eventId(),
                evento.type()
        );

        PedidoKafkaEvent eventoKafka = new PedidoKafkaEvent(
                evento.eventId(),
                evento.type(),
                evento.timestamp(),
                evento.traceId(),
                evento.correlationId(),
                evento.payload()
        );

        // Esperamos confirmación del envío a Kafka.
        kafkaProducer.publicar(eventoKafka).join();

        log.info(
                "Evento transferido correctamente a Kafka | EventId: {}",
                evento.eventId()
        );
    }

    private boolean esEventoValido(ComandoEnvelope evento) {

        return evento != null
                && evento.eventId() != null
                && !evento.eventId().isBlank()
                && evento.type() != null
                && !evento.type().isBlank()
                && evento.timestamp() != null
                && evento.payload() != null
                && !evento.payload().isNull();
    }
}