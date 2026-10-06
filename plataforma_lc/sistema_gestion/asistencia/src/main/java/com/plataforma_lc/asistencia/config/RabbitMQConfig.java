package com.plataforma_lc.asistencia.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Dominio: asistencia ──────────────────────────────────────────────────
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

    // ── Dominio: evaluaciones (consumer idempotente) ─────────────────────────
    @Value("${rabbitmq.queue.evaluaciones}")
    private String evalQueueName;

    @Value("${rabbitmq.queue.evaluaciones.dlq}")
    private String evalDlqName;

    @Value("${rabbitmq.exchange.evaluaciones}")
    private String evalExchangeName;

    @Value("${rabbitmq.exchange.evaluaciones.dlx}")
    private String evalDlxName;

    @Value("${rabbitmq.routingkey.evaluaciones}")
    private String evalRoutingKey;

    @Value("${rabbitmq.routingkey.evaluaciones.dlq}")
    private String evalDlqRoutingKey;

    // ════════════════════════════════════════════════════════════════════════
    // Beans: Dominio ASISTENCIA
    // ════════════════════════════════════════════════════════════════════════

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

    // ════════════════════════════════════════════════════════════════════════
    // Beans: Dominio EVALUACIONES (consumer idempotente)
    // Se declaran aquí para garantizar que la cola existe en el broker
    // independientemente del orden de arranque de los servicios.
    // ════════════════════════════════════════════════════════════════════════

    @Bean
    public Queue evaluacionesQueue() {
        return QueueBuilder.durable(evalQueueName)
                .withArgument("x-dead-letter-exchange", evalDlxName)
                .withArgument("x-dead-letter-routing-key", evalDlqRoutingKey)
                .build();
    }

    @Bean
    public Queue evaluacionesDlq() {
        return QueueBuilder.durable(evalDlqName).build();
    }

    @Bean
    public TopicExchange evaluacionesExchange() {
        return new TopicExchange(evalExchangeName);
    }

    @Bean
    public TopicExchange evaluacionesDlx() {
        return new TopicExchange(evalDlxName);
    }

    @Bean
    public Binding evaluacionesBinding(
            @Qualifier("evaluacionesQueue") Queue evaluacionesQueue,
            @Qualifier("evaluacionesExchange") TopicExchange evaluacionesExchange) {
        return BindingBuilder.bind(evaluacionesQueue).to(evaluacionesExchange).with(evalRoutingKey);
    }

    @Bean
    public Binding evaluacionesDlqBinding(
            @Qualifier("evaluacionesDlq") Queue evaluacionesDlq,
            @Qualifier("evaluacionesDlx") TopicExchange evaluacionesDlx) {
        return BindingBuilder.bind(evaluacionesDlq).to(evaluacionesDlx).with(evalDlqRoutingKey);
    }
}