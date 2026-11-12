package com.plataforma_lc.adminRabbitMQ.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

class RabbitMQAdminConfigTest {

    @Test
    void rabbitAdmin_creaInstanciaCorrectamente() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        RabbitMQAdminConfig config = new RabbitMQAdminConfig();

        RabbitAdmin admin = config.rabbitAdmin(connectionFactory);

        assertNotNull(admin);
    }
}