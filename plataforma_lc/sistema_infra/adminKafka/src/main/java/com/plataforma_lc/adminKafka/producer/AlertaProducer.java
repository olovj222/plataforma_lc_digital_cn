package com.plataforma_lc.adminKafka.producer;

import com.plataforma_lc.adminKafka.dto.AlertaEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Productor Kafka para eventos de alertas críticas.
 * Publica mensajes al tópico "plataforma.alertas.criticas" con acks=all.
 * Utiliza callbacks asíncronos para registrar éxito o errores.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaProducer {

    private static final String TOPIC = "plataforma.alertas.criticas";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publica un evento de alerta al tópico correspondiente.
     * La clave de partición se basa en el alertaId para garantizar
     * que actualizaciones de la misma alerta vayan a la misma partición.
     *
     * @param evento DTO con los datos de la alerta
     */
    public void publicar(AlertaEventDTO evento) {
        String key = evento.getAlertaId();
        log.info("[ALERTA-PRODUCER] Publicando alerta: alertaId={}, tipo={}, nivel={}, origen={}",
                evento.getAlertaId(), evento.getTipoAlerta(), evento.getNivel(), evento.getOrigenServicio());

        kafkaTemplate.send(TOPIC, key, evento)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[ALERTA-PRODUCER] ✅ Alerta publicada exitosamente -> topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("[ALERTA-PRODUCER] ❌ Error al publicar alerta: alertaId={}, nivel={} -> {}",
                                evento.getAlertaId(), evento.getNivel(), ex.getMessage(), ex);
                    }
                });
    }
}

