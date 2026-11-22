
package cl.duoc.ms_pedidos360_audit.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import cl.duoc.ms_pedidos360_audit.dto.PedidoKafkaEvent;

@Configuration
public class KafkaAuditErrorConfig {

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, PedidoKafkaEvent> kafkaTemplate,
            @Value("${app.kafka.topics.dlt}") String dltTopic) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(dltTopic, -1)
                );

        recoverer.setFailIfSendResultIsError(true);

        return recoverer;
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer recoverer) {

        // 2 reintentos adicionales después del intento inicial.
        FixedBackOff backOff = new FixedBackOff(1000L, 2L);

        return new DefaultErrorHandler(
                recoverer,
                backOff
        );
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PedidoKafkaEvent>
            kafkaListenerContainerFactory(
                    ConsumerFactory<String, PedidoKafkaEvent> consumerFactory,
                    CommonErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, PedidoKafkaEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}
