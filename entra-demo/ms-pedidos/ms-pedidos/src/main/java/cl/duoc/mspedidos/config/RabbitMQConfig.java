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
    public static final String Q_KITCHEN = "q.cmd.kitchen";
    public static final String Q_INVOICE = "q.cmd.invoice";

    @Bean
    public Queue qEmail() {
        return QueueBuilder.durable(Q_EMAIL).build();
    }

    @Bean
    public Queue qKitchen() {
        return QueueBuilder.durable(Q_KITCHEN).build();
    }

    @Bean
    public Queue qInvoice() {
        return QueueBuilder.durable(Q_INVOICE).build();
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Binding bEmail() {
        return BindingBuilder.bind(qEmail()).to(directExchange()).with("email.send");
    }

    @Bean
    public Binding bKitchen() {
        return BindingBuilder.bind(qKitchen()).to(directExchange()).with("kitchen.ticket");
    }

    @Bean
    public Binding bInvoice() {
        return BindingBuilder.bind(qInvoice()).to(directExchange()).with("invoice.gen");
    }

    @Bean
    public Binding bEmailTopic() {
        return BindingBuilder.bind(qEmail()).to(topicExchange()).with("email.*");
    }

    @Bean
    public Binding bKitchenTopic() {
        return BindingBuilder.bind(qKitchen()).to(topicExchange()).with("kitchen.#");
    }

    @Bean
    public Binding bInvoiceTopic() {
        return BindingBuilder.bind(qInvoice()).to(topicExchange()).with("invoice.*");
    }
}