package com.plataforma_lc.adminRabbitMQ.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para crear un Binding (unión entre queue y exchange) vía adminRabbitMQ.
 */
public class BindingRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    private String queueName;

    @NotBlank(message = "El nombre del exchange es obligatorio")
    private String exchangeName;

    @NotBlank(message = "El routing key es obligatorio")
    private String routingKey;

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getExchangeName() { return exchangeName; }
    public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }

    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
}
