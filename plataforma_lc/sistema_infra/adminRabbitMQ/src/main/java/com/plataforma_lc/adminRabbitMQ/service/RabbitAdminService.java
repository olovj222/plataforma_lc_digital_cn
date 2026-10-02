package com.plataforma_lc.adminRabbitMQ.service;

import com.plataforma_lc.adminRabbitMQ.dto.QueueRequestDTO;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public void createQueue(QueueRequestDTO dto) {
        Queue queue = new Queue(dto.getName(), dto.isDurable(), dto.isExclusive(), dto.isAutoDelete());
        amqpAdmin.declareQueue(queue);
    }

    public boolean deleteQueue(String queueName) {
        return amqpAdmin.deleteQueue(queueName);
    }
}
