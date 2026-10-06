package com.plataforma_lc.justificativos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.justificativos}")
    private String queueName;

    @Value("${rabbitmq.queue.justificativos.dlq}")
    private String dlqName;

    @Value("${rabbitmq.exchange.asistencia}")
    private String exchangeName;

    @Value("${rabbitmq.exchange.dlx}")
    private String dlxName;

    @Value("${rabbitmq.routingkey.asistencia}")
    private String routingKey;

    @Value("${rabbitmq.routingkey.asistencia.dlq}")
    private String dlqRoutingKey;

    // 1. Cola principal propia de Justificativos
    @Bean
    public Queue justificativosQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    // 2. Cola para Mensajes Fallidos (DLQ)
    @Bean
    public Queue justificativosDlq() {
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

    // 4. Bindings con @Qualifier explícitos
    @Bean
    public Binding justificativosBinding(
            @Qualifier("justificativosQueue") Queue justificativosQueue,
            @Qualifier("asistenciaExchange") TopicExchange asistenciaExchange) {
        return BindingBuilder.bind(justificativosQueue).to(asistenciaExchange).with(routingKey);
    }

    @Bean
    public Binding justificativosDlqBinding(
            @Qualifier("justificativosDlq") Queue justificativosDlq,
            @Qualifier("deadLetterExchange") TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(justificativosDlq).to(deadLetterExchange).with(dlqRoutingKey);
    }

    // 5. Conversor JSON
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}