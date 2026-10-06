package com.plataforma_lc.adminRabbitMQ.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans AMQP para el microservicio adminRabbitMQ.
 * Expone RabbitAdmin para gestión programática de recursos en el broker.
 */
@Configuration
public class RabbitMQAdminConfig {

    /**
     * RabbitAdmin es el componente de Spring AMQP que permite gestionar
     * recursos de RabbitMQ (colas, exchanges, bindings) en tiempo de ejecución.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
