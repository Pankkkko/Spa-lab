package cl.duoc.ms_pedidos360_report.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQReportConfig {

    public static final String EXCHANGE_TOPIC = "cmd.topic";

    public static final String QUEUE_REPORT_PEDIDOS =
            "q.report.pedidos";

    public static final String ROUTING_KEY_PEDIDOS =
            "pedidos.estado.actualizado";

    public static final String EXCHANGE_REPORT_DLX =
            "report.dead.dlx";

    public static final String QUEUE_REPORT_DLQ =
            "q.report.pedidos.dlq";

    public static final String ROUTING_KEY_REPORT_DLQ =
            "report.pedidos.failed";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }

    @Bean
    public DirectExchange reportDeadLetterExchange() {
        return new DirectExchange(EXCHANGE_REPORT_DLX, true, false);
    }

    @Bean
    public Queue reportPedidosQueue() {

        return QueueBuilder
                .durable(QUEUE_REPORT_PEDIDOS)
                .deadLetterExchange(EXCHANGE_REPORT_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_REPORT_DLQ)
                .build();
    }

    @Bean
    public Queue reportDeadLetterQueue() {
        return new Queue(QUEUE_REPORT_DLQ, true);
    }

    @Bean
    public Binding reportPedidosBinding(
            Queue reportPedidosQueue,
            TopicExchange topicExchange) {

        return BindingBuilder
                .bind(reportPedidosQueue)
                .to(topicExchange)
                .with(ROUTING_KEY_PEDIDOS);
    }

    @Bean
    public Binding reportDeadLetterBinding(
            Queue reportDeadLetterQueue,
            DirectExchange reportDeadLetterExchange) {

        return BindingBuilder
                .bind(reportDeadLetterQueue)
                .to(reportDeadLetterExchange)
                .with(ROUTING_KEY_REPORT_DLQ);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}