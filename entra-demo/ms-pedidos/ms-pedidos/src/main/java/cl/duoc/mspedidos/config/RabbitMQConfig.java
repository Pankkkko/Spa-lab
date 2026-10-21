
package cl.duoc.mspedidos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    public static final String Q_EMAIL = "q.cmd.email";
    public static final String Q_PEDIDOS = "q.cmd.pedidos";
    public static final String Q_INVOICE = "q.cmd.invoice";

    public static final String Q_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String Q_PEDIDOS_DLQ = "q.cmd.pedidos.dlq";
    public static final String Q_INVOICE_DLQ = "q.cmd.invoice.dlq";

    public static final String KEY_EMAIL = "email.send";
    public static final String KEY_PEDIDOS = "pedidos.procesar";
    public static final String KEY_INVOICE = "invoice.gen";

    public static final String PATTERN_EMAIL = "email.*";
    public static final String PATTERN_PEDIDOS = "pedidos.#";
    public static final String PATTERN_INVOICE = "invoice.*";

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

    // Colas principales con configuración DLX compatible
    @Bean
    public Queue colaEmail() {
        return QueueBuilder.durable(Q_EMAIL)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_EMAIL)
                .build();
    }

    @Bean
    public Queue colaPedidos() {
        return QueueBuilder.durable(Q_PEDIDOS)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_PEDIDOS)
                .build();
    }

    @Bean
    public Queue colaInvoice() {
        return QueueBuilder.durable(Q_INVOICE)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_INVOICE)
                .build();
    }

    // Dead Letter Queues
    @Bean
    public Queue colaEmailDlq() {
        return QueueBuilder.durable(Q_EMAIL_DLQ).build();
    }

    @Bean
    public Queue colaPedidosDlq() {
        return QueueBuilder.durable(Q_PEDIDOS_DLQ).build();
    }

    @Bean
    public Queue colaInvoiceDlq() {
        return QueueBuilder.durable(Q_INVOICE_DLQ).build();
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
}
