package com.plataforma_lc.adminRabbitMQ.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para crear un Exchange de forma programática vía adminRabbitMQ.
 */
public class ExchangeRequestDTO {

    @NotBlank(message = "El nombre del exchange es obligatorio")
    private String name;

    /**
     * Tipo de exchange: "direct", "topic" o "fanout".
     * Por defecto: "direct"
     */
    private String type = "direct";

    private boolean durable = true;
    private boolean autoDelete = false;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isDurable() { return durable; }
    public void setDurable(boolean durable) { this.durable = durable; }

    public boolean isAutoDelete() { return autoDelete; }
    public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }
}
