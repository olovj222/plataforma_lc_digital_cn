package com.plataforma_lc.adminRabbitMQ.service;

import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para controlar dinámicamente los Listener Containers de RabbitMQ.
 *
 * En Spring AMQP 3.x (Spring Boot 3.x), los métodos pause()/resume() fueron
 * eliminados de AbstractMessageListenerContainer. La forma estándar es usar:
 *  - stop()  → Detiene el consumo (equivalente a pausar)
 *  - start() → Reanuda el consumo
 *
 * Los IDs de listener se definen con el atributo 'id' en @RabbitListener.
 */
@Service
public class ListenerManagementService {

    private final RabbitListenerEndpointRegistry registry;

    public ListenerManagementService(RabbitListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    /**
     * Detiene el consumo de un listener por su ID.
     * El container se desregistra del broker; los mensajes se acumulan en la cola.
     * Equivalente funcional al "pause" en Spring AMQP 3.x.
     */
    public String pauseListener(String listenerId) {
        MessageListenerContainer container = registry.getListenerContainer(listenerId);
        if (container == null) {
            return "Listener '" + listenerId + "' no encontrado.";
        }
        if (!container.isRunning()) {
            return "Listener '" + listenerId + "' ya estaba detenido.";
        }
        container.stop();
        return "Listener '" + listenerId + "' detenido correctamente.";
    }

    /**
     * Reanuda el consumo de un listener previamente detenido.
     */
    public String resumeListener(String listenerId) {
        MessageListenerContainer container = registry.getListenerContainer(listenerId);
        if (container == null) {
            return "Listener '" + listenerId + "' no encontrado.";
        }
        if (container.isRunning()) {
            return "Listener '" + listenerId + "' ya estaba activo.";
        }
        container.start();
        return "Listener '" + listenerId + "' reanudado correctamente.";
    }

    /**
     * Retorna los IDs de todos los listeners registrados en este microservicio.
     * Usa getListenerContainerIds() del registry, disponible en Spring AMQP 3.x.
     */
    public List<String> getAllListenerIds() {
        return List.copyOf(registry.getListenerContainerIds());
    }
}

