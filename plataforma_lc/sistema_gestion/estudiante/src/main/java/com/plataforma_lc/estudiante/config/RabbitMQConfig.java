package com.plataforma_lc.estudiante.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración RabbitMQ del MS estudiante.
 * Declara anotaciones.queue + DLQ de forma idempotente:
 * garantiza que la cola existe en el broker independientemente
 * del orden de arranque respecto al MS anotaciones (productor).
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.anotaciones}")
    private String queueName;

    @Value("${rabbitmq.queue.anotaciones.dlq}")
    private String dlqName;

    @Value("${rabbitmq.exchange.anotaciones}")
    private String exchangeName;

    @Value("${rabbitmq.exchange.anotaciones.dlx}")
    private String dlxName;

    @Value("${rabbitmq.routingkey.anotaciones}")
    private String routingKey;

    @Value("${rabbitmq.routingkey.anotaciones.dlq}")
    private String dlqRoutingKey;

    // 1. Cola principal con DLX
    @Bean
    public Queue anotacionesQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    // 2. Dead Letter Queue
    @Bean
    public Queue anotacionesDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    // 3. Exchanges
    @Bean
    public TopicExchange anotacionesExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public TopicExchange anotacionesDlx() {
        return new TopicExchange(dlxName);
    }

    // 4. Bindings
    @Bean
    public Binding anotacionesBinding(
            @Qualifier("anotacionesQueue") Queue anotacionesQueue,
            @Qualifier("anotacionesExchange") TopicExchange anotacionesExchange) {
        return BindingBuilder.bind(anotacionesQueue).to(anotacionesExchange).with(routingKey);
    }

    @Bean
    public Binding anotacionesDlqBinding(
            @Qualifier("anotacionesDlq") Queue anotacionesDlq,
            @Qualifier("anotacionesDlx") TopicExchange anotacionesDlx) {
        return BindingBuilder.bind(anotacionesDlq).to(anotacionesDlx).with(dlqRoutingKey);
    }

    // 5. Conversor JSON
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
