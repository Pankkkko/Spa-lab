package cl.duoc.ms_pedidos360_notify.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {

    // Exchanges
    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    // Colas principales
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_PEDIDOS = "q.cmd.pedidos";
    public static final String QUEUE_INVOICE = "q.cmd.invoice";

    // Dead-letter queues
    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String QUEUE_PEDIDOS_DLQ = "q.cmd.pedidos.dlq";
    public static final String QUEUE_INVOICE_DLQ = "q.cmd.invoice.dlq";

    // Routing keys
    public static final String KEY_EMAIL = "email.send";
    public static final String KEY_PEDIDOS = "pedidos.procesar";
    public static final String KEY_INVOICE = "invoice.gen";

    // Patrones topic
    public static final String PATTERN_EMAIL = "email.*";
    public static final String PATTERN_PEDIDOS = "pedidos.#";
    public static final String PATTERN_INVOICE = "invoice.*";

    // Reintentos locales, sin crear una séptima cola
    public static final int MAX_INTENTOS = 3;
    public static final int ESPERA_REINTENTO_MS = 5000;

    @Bean
    public DirectExchange cmdDirectExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    @Bean
    public TopicExchange cmdTopicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }

    @Bean
    public DirectExchange cmdDeadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    // Colas principales con su DLX y routing key de destino
    @Bean
    public Queue colaEmail() {
        return QueueBuilder.durable(QUEUE_EMAIL)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_EMAIL)
                .build();
    }

    @Bean
    public Queue colaPedidos() {
        return QueueBuilder.durable(QUEUE_PEDIDOS)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_PEDIDOS)
                .build();
    }

    @Bean
    public Queue colaInvoice() {
        return QueueBuilder.durable(QUEUE_INVOICE)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_INVOICE)
                .build();
    }

    // DLQ
    @Bean
    public Queue colaEmailDlq() {
        return QueueBuilder.durable(QUEUE_EMAIL_DLQ).build();
    }

    @Bean
    public Queue colaPedidosDlq() {
        return QueueBuilder.durable(QUEUE_PEDIDOS_DLQ).build();
    }

    @Bean
    public Queue colaInvoiceDlq() {
        return QueueBuilder.durable(QUEUE_INVOICE_DLQ).build();
    }

    // Bindings direct
    @Bean
    public Binding emailDirectBinding() {
        return BindingBuilder.bind(colaEmail())
                .to(cmdDirectExchange()).with(KEY_EMAIL);
    }

    @Bean
    public Binding pedidosDirectBinding() {
        return BindingBuilder.bind(colaPedidos())
                .to(cmdDirectExchange()).with(KEY_PEDIDOS);
    }

    @Bean
    public Binding invoiceDirectBinding() {
        return BindingBuilder.bind(colaInvoice())
                .to(cmdDirectExchange()).with(KEY_INVOICE);
    }

    // Bindings topic
    @Bean
    public Binding emailTopicBinding() {
        return BindingBuilder.bind(colaEmail())
                .to(cmdTopicExchange()).with(PATTERN_EMAIL);
    }

    @Bean
    public Binding pedidosTopicBinding() {
        return BindingBuilder.bind(colaPedidos())
                .to(cmdTopicExchange()).with(PATTERN_PEDIDOS);
    }

    @Bean
    public Binding invoiceTopicBinding() {
        return BindingBuilder.bind(colaInvoice())
                .to(cmdTopicExchange()).with(PATTERN_INVOICE);
    }

    // Bindings DLQ
    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(colaEmailDlq())
                .to(cmdDeadLetterExchange()).with(KEY_EMAIL);
    }

    @Bean
    public Binding pedidosDlqBinding() {
        return BindingBuilder.bind(colaPedidosDlq())
                .to(cmdDeadLetterExchange()).with(KEY_PEDIDOS);
    }

    @Bean
    public Binding invoiceDlqBinding() {
        return BindingBuilder.bind(colaInvoiceDlq())
                .to(cmdDeadLetterExchange()).with(KEY_INVOICE);
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
