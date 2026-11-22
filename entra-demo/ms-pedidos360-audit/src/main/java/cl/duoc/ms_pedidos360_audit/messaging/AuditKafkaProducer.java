
package cl.duoc.ms_pedidos360_audit.messaging;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import cl.duoc.ms_pedidos360_audit.dto.AuditTimelineEvent;

@Service
public class AuditKafkaProducer {

    private static final Logger log =
            LoggerFactory.getLogger(AuditKafkaProducer.class);

    private final KafkaTemplate<String, AuditTimelineEvent> kafkaTemplate;

    @Value("${app.kafka.topics.audit}")
    private String auditTopic;

    public AuditKafkaProducer(
            KafkaTemplate<String, AuditTimelineEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<Void> publicar(AuditTimelineEvent evento) {

        // La clave permite agrupar los registros por entidad/pedido.
        String kafkaKey = evento.entidadId();

        return kafkaTemplate.send(
                auditTopic,
                kafkaKey,
                evento
        ).thenAccept(resultado -> {

            log.info(
                    "Evento de auditoría publicado | Topic: {} | Partition: {} | Offset: {} | Pedido: {}",
                    resultado.getRecordMetadata().topic(),
                    resultado.getRecordMetadata().partition(),
                    resultado.getRecordMetadata().offset(),
                    kafkaKey
            );

        }).exceptionally(error -> {

            log.error(
                    "Error publicando evento de auditoría {}",
                    evento.eventId(),
                    error
            );

            throw new java.util.concurrent.CompletionException(error);
        });
    }
}
