package com.plataforma_lc.adminRabbitMQ.service;

import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para controlar dinámicamente los Listener Containers de RabbitMQ.
 *
 * Métodos clave:
 *  - pause()  → Pausa el consumo (la conexión sigue abierta)
 *  - resume() → Reanuda el consumo
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
     * Pausa el consumo de un listener por su ID.
     * La conexión al broker permanece abierta; los mensajes se acumulan en la cola.
     */
    public String pauseListener(String listenerId) {
        MessageListenerContainer container = registry.getListenerContainer(listenerId);
        if (container == null) {
            return "Listener '" + listenerId + "' no encontrado.";
        }
        if (!container.isRunning()) {
            return "Listener '" + listenerId + "' ya estaba detenido.";
        }
        container.pause();
        return "Listener '" + listenerId + "' pausado correctamente.";
    }

    /**
     * Reanuda el consumo de un listener previamente pausado.
     */
    public String resumeListener(String listenerId) {
        MessageListenerContainer container = registry.getListenerContainer(listenerId);
        if (container == null) {
            return "Listener '" + listenerId + "' no encontrado.";
        }
        container.resume();
        return "Listener '" + listenerId + "' reanudado correctamente.";
    }

    /**
     * Retorna los IDs de todos los listeners registrados en este microservicio.
     */
    public List<String> getAllListenerIds() {
        Collection<MessageListenerContainer> containers = registry.getListenerContainers();
        return containers.stream()
                .map(MessageListenerContainer::getListenerId)
                .collect(Collectors.toList());
    }
}
