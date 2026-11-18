package cl.duoc.ms_pedidos360_report.messaging;

import cl.duoc.ms_pedidos360_report.dto.PedidoKafkaEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PedidoKafkaProducer {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoKafkaProducer.class);

    private final KafkaTemplate<String, PedidoKafkaEvent> kafkaTemplate;

    @Value("${app.kafka.topics.orders}")
    private String ordersTopic;

    public PedidoKafkaProducer(
            KafkaTemplate<String, PedidoKafkaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<Void> publicar(PedidoKafkaEvent evento) {

        return kafkaTemplate.send(
                ordersTopic,
                evento.eventId(),
                evento
        ).thenAccept(resultado -> {

            log.info(
                    "Evento publicado en Kafka | Topic: {} | Partition: {} | Offset: {} | EventId: {}",
                    resultado.getRecordMetadata().topic(),
                    resultado.getRecordMetadata().partition(),
                    resultado.getRecordMetadata().offset(),
                    evento.eventId()
            );

        }).exceptionally(error -> {

            log.error(
                    "Error publicando evento {} en Kafka",
                    evento.eventId(),
                    error
            );

            throw new java.util.concurrent.CompletionException(error);
        });
    }
}