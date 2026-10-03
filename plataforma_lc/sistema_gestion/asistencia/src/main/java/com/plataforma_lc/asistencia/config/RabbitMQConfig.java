package com.plataforma_lc.asistencia.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.asistencia}")
    private String queueName;

    @Value("${rabbitmq.queue.asistencia.dlq}")
    private String dlqName;

    @Value("${rabbitmq.exchange.asistencia}")
    private String exchangeName;

    @Value("${rabbitmq.exchange.dlx}")
    private String dlxName;

    @Value("${rabbitmq.routingkey.asistencia}")
    private String routingKey;

    @Value("${rabbitmq.routingkey.asistencia.dlq}")
    private String dlqRoutingKey;

    // 1. Cola principal asociada a Dead Letter Exchange
    @Bean
    public Queue asistenciaQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    // 2. Cola para Mensajes Fallidos (DLQ)
    @Bean
    public Queue asistenciaDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    // 3. Exchanges
    @Bean
    public TopicExchange asistenciaExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(dlxName);
    }

    // 4. Bindings con @Qualifier explícitos para evitar conflictos
    @Bean
    public Binding asistenciaBinding(
            @Qualifier("asistenciaQueue") Queue asistenciaQueue, 
            @Qualifier("asistenciaExchange") TopicExchange asistenciaExchange) {
        return BindingBuilder.bind(asistenciaQueue).to(asistenciaExchange).with(routingKey);
    }

    @Bean
    public Binding dlqBinding(
            @Qualifier("asistenciaDlq") Queue asistenciaDlq, 
            @Qualifier("deadLetterExchange") TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(asistenciaDlq).to(deadLetterExchange).with(dlqRoutingKey);
    }

    // 5. Conversor JSON
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}