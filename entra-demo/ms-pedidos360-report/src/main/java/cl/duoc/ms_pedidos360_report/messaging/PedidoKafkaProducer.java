package cl.duoc.ms_pedidos360_report.messaging;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import cl.duoc.ms_pedidos360_report.dto.PedidoKafkaEvent;

@Service
public class PedidoKafkaProducer {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoKafkaProducer.class);

    private final KafkaTemplate<String, PedidoKafkaEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.orders}")
    private String ordersTopic;

    public PedidoKafkaProducer(
            KafkaTemplate<String, PedidoKafkaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<Void> publicar(PedidoKafkaEvent evento) {

        String kafkaKey = evento.payload()
                .path("pedidoId")
                .asText(evento.eventId());

        return kafkaTemplate.send(
                ordersTopic,
                kafkaKey,
                evento
        ).thenAccept(resultado -> {

            log.info(
                    "Evento publicado | Topic: {} | Partition: {} | Offset: {} | Pedido: {}",
                    resultado.getRecordMetadata().topic(),
                    resultado.getRecordMetadata().partition(),
                    resultado.getRecordMetadata().offset(),
                    kafkaKey
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