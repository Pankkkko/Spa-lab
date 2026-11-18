package cl.duoc.ms_pedidos360_report.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
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

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }

    @Bean
    public Queue reportPedidosQueue() {
        return new Queue(QUEUE_REPORT_PEDIDOS, true);
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
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}