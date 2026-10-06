package com.plataforma_lc.evaluaciones.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.evaluaciones}")
    private String queueName;

    @Value("${rabbitmq.queue.evaluaciones.dlq}")
    private String dlqName;

    @Value("${rabbitmq.exchange.evaluaciones}")
    private String exchangeName;

    @Value("${rabbitmq.exchange.evaluaciones.dlx}")
    private String dlxName;

    @Value("${rabbitmq.routingkey.evaluaciones}")
    private String routingKey;

    @Value("${rabbitmq.routingkey.evaluaciones.dlq}")
    private String dlqRoutingKey;

    // 1. Cola principal asociada a Dead Letter Exchange
    @Bean
    public Queue evaluacionesQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    // 2. Cola para Mensajes Fallidos (DLQ)
    @Bean
    public Queue evaluacionesDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    // 3. Exchanges
    @Bean
    public TopicExchange evaluacionesExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public TopicExchange evaluacionesDlx() {
        return new TopicExchange(dlxName);
    }

    // 4. Bindings
    @Bean
    public Binding evaluacionesBinding(
            @Qualifier("evaluacionesQueue") Queue evaluacionesQueue,
            @Qualifier("evaluacionesExchange") TopicExchange evaluacionesExchange) {
        return BindingBuilder.bind(evaluacionesQueue).to(evaluacionesExchange).with(routingKey);
    }

    @Bean
    public Binding evaluacionesDlqBinding(
            @Qualifier("evaluacionesDlq") Queue evaluacionesDlq,
            @Qualifier("evaluacionesDlx") TopicExchange evaluacionesDlx) {
        return BindingBuilder.bind(evaluacionesDlq).to(evaluacionesDlx).with(dlqRoutingKey);
    }

    // 5. Conversor JSON
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
