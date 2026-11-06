package com.plataforma_lc.adminKafka.producer;

import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Productor Kafka para el stream de analítica de asistencia.
 * Publica mensajes al tópico "asistencia.analitica.stream" con acks=all.
 * Utiliza callbacks asíncronos para registrar éxito o errores.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnaliticaProducer {

    private static final String TOPIC = "asistencia.analitica.stream";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publica un registro de analítica de asistencia.
     * La clave de partición se basa en cursoId para agrupar registros
     * del mismo curso en la misma partición, permitiendo orden secuencial.
     *
     * @param dto DTO con los datos de analítica
     */
    public void publicar(AnaliticaStreamDTO dto) {
        String key = String.valueOf(dto.getCursoId());
        log.info("[ANALITICA-PRODUCER] Publicando stream: cursoId={}, estudianteId={}, estado={}",
                dto.getCursoId(), dto.getEstudianteId(), dto.getEstado());

        kafkaTemplate.send(TOPIC, key, dto)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[ANALITICA-PRODUCER] ✅ Stream publicado exitosamente -> topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("[ANALITICA-PRODUCER] ❌ Error al publicar stream de analítica: cursoId={}, estudianteId={} -> {}",
                                dto.getCursoId(), dto.getEstudianteId(), ex.getMessage(), ex);
                    }
                });
    }
}

