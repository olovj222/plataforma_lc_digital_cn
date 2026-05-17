/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.eurekaServer.listeners;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaEventListener {

    private static final Logger log = LoggerFactory.getLogger(EurekaEventListener.class);

    @EventListener
    public void onInstanceCanceled(EurekaInstanceCanceledEvent event) {
        log.error("[SERVICIO CAÍDO] Servicio: {} | Instancia: {} | Timestamp: {}",
            event.getAppName(),
            event.getServerId(),
                LocalDateTime.now()
        );
    }

    @EventListener
    public void onInstanceRegistered(EurekaInstanceRegisteredEvent event) {
        log.info("[SERVICIO REGISTRADO] Servicio: {} | Instancia: {} | Timestamp: {}",
            event.getInstanceInfo().getAppName(),
            event.getInstanceInfo().getInstanceId(),
            LocalDateTime.now()
        );
    }
    
    @EventListener
    public void onInstanceRenewed(EurekaInstanceRenewedEvent event) {
        log.info("[SERVICIO ACTIVO] Servicio: {} | Instancia: {} | Timestamp: {}",
            event.getAppName(),
            event.getServerId(),
            LocalDateTime.now()
        );
    }
}
