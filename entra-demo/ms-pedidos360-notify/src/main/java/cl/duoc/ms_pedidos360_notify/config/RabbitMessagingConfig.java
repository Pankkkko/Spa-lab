package cl.duoc.ms_pedidos360_notify.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {

    // Exchange principal de eventos
    public static final String EXCHANGE_PEDIDOS =
            "pedidos360.events";

    // Cola consumida por este microservicio
    public static final String QUEUE_NOTIFICACIONES =
            "notificaciones.pedidos";

    // Routing key del evento de creación
    public static final String ROUTING_KEY_PEDIDO_CREADO =
            "pedido.creado";

    // Exchange y cola para mensajes fallidos
    public static final String EXCHANGE_DLQ =
            "pedidos360.notify.dlx";

    public static final String QUEUE_DLQ =
            "notificaciones.pedidos.dlq";

    public static final String ROUTING_KEY_DLQ =
            "pedido.creado.dlq";

    // Cola de reintento
    public static final String QUEUE_REINTENTOS =
            "notificaciones.pedidos.retry";

    public static final String HEADER_REINTENTOS =
            "x-retry-count";

    public static final int MAX_INTENTOS = 3;
    public static final int ESPERA_REINTENTO_MS = 5000;

    @Bean
    public DirectExchange exchangePedidos() {
        return new DirectExchange(EXCHANGE_PEDIDOS, true, false);
    }

    @Bean
    public DirectExchange exchangeDlq() {
        return new DirectExchange(EXCHANGE_DLQ, true, false);
    }

    @Bean
    public Queue colaNotificaciones() {
        return QueueBuilder.durable(QUEUE_NOTIFICACIONES)
                .withArgument(
                        "x-dead-letter-exchange",
                        EXCHANGE_DLQ
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        ROUTING_KEY_DLQ
                )
                .build();
    }

    @Bean
    public Queue colaDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Queue colaReintentos() {
        return QueueBuilder.durable(QUEUE_REINTENTOS)
                .withArgument(
                        "x-message-ttl",
                        ESPERA_REINTENTO_MS
                )
                .withArgument(
                        "x-dead-letter-exchange",
                        EXCHANGE_PEDIDOS
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        ROUTING_KEY_PEDIDO_CREADO
                )
                .build();
    }

    @Bean
    public Binding bindingNotificaciones() {
        return BindingBuilder
                .bind(colaNotificaciones())
                .to(exchangePedidos())
                .with(ROUTING_KEY_PEDIDO_CREADO);
    }

    @Bean
    public Binding bindingDlq() {
        return BindingBuilder
                .bind(colaDlq())
                .to(exchangeDlq())
                .with(ROUTING_KEY_DLQ);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter jsonMessageConverter) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);

        return factory;
    }
}