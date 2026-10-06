package com.plataforma_lc.adminRabbitMQ.service;

import com.plataforma_lc.adminRabbitMQ.dto.BindingRequestDTO;
import com.plataforma_lc.adminRabbitMQ.dto.ExchangeRequestDTO;
import com.plataforma_lc.adminRabbitMQ.dto.QueueRequestDTO;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

/**
 * Servicio de gestión programática de recursos RabbitMQ mediante RabbitAdmin.
 * Permite crear/eliminar colas, exchanges y bindings en tiempo de ejecución.
 */
@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;
    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin, RabbitAdmin rabbitAdmin) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitAdmin = rabbitAdmin;
    }

    // ════════════════════════════════════════════════════════════════════════
    // COLAS
    // ════════════════════════════════════════════════════════════════════════

    public void createQueue(QueueRequestDTO dto) {
        Queue queue = new Queue(dto.getName(), dto.isDurable(), dto.isExclusive(), dto.isAutoDelete());
        amqpAdmin.declareQueue(queue);
    }

    public boolean deleteQueue(String queueName) {
        return amqpAdmin.deleteQueue(queueName);
    }

    /**
     * Obtiene información de una cola: nombre, cantidad de mensajes y consumidores.
     * Retorna null si la cola no existe.
     */
    public QueueInformation getQueueInfo(String queueName) {
        return rabbitAdmin.getQueueInfo(queueName);
    }

    /**
     * Elimina todos los mensajes de una cola sin eliminarla.
     * Retorna el número de mensajes purgados.
     */
    public int purgeQueue(String queueName) {
        return rabbitAdmin.purgeQueue(queueName);
    }

    // ════════════════════════════════════════════════════════════════════════
    // EXCHANGES
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Crea un exchange del tipo especificado en el DTO (direct, topic, fanout).
     */
    public void createExchange(ExchangeRequestDTO dto) {
        Exchange exchange = switch (dto.getType().toLowerCase()) {
            case "topic"  -> new TopicExchange(dto.getName(), dto.isDurable(), dto.isAutoDelete());
            case "fanout" -> new FanoutExchange(dto.getName(), dto.isDurable(), dto.isAutoDelete());
            default       -> new DirectExchange(dto.getName(), dto.isDurable(), dto.isAutoDelete());
        };
        amqpAdmin.declareExchange(exchange);
    }

    public boolean deleteExchange(String exchangeName) {
        return amqpAdmin.deleteExchange(exchangeName);
    }

    // ════════════════════════════════════════════════════════════════════════
    // BINDINGS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Crea un binding entre una cola y un exchange con la routing key indicada.
     * El exchange debe ser de tipo direct o topic para que el routing key sea relevante.
     */
    public void createBinding(BindingRequestDTO dto) {
        Binding binding = BindingBuilder
                .bind(new Queue(dto.getQueueName()))
                .to(new DirectExchange(dto.getExchangeName()))
                .with(dto.getRoutingKey());
        amqpAdmin.declareBinding(binding);
    }

    public void removeBinding(BindingRequestDTO dto) {
        Binding binding = BindingBuilder
                .bind(new Queue(dto.getQueueName()))
                .to(new DirectExchange(dto.getExchangeName()))
                .with(dto.getRoutingKey());
        amqpAdmin.removeBinding(binding);
    }
}
