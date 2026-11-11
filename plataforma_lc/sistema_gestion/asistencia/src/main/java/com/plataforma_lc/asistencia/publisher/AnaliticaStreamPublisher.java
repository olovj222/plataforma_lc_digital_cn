package com.plataforma_lc.asistencia.publisher;

import com.plataforma_lc.asistencia.dto.AnaliticaStreamDTO;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

@Component
public class AnaliticaStreamPublisher {

    private static final Logger log = LoggerFactory.getLogger(AnaliticaStreamPublisher.class);

    static final String TYPE_ID_HEADER = "__TypeId__";
    static final String CONSUMER_DTO_CLASS = "com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.analitica-stream:asistencia.analitica.stream}")
    private String topic;

    @Value("${kafka.analitica.habilitada:true}")
    private boolean habilitada;

    public AnaliticaStreamPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(AnaliticaStreamDTO dto) {
        if (!habilitada) {
            return;
        }

        try {
            String key = String.valueOf(dto.getCursoId());
            
            Message<AnaliticaStreamDTO> message = MessageBuilder
                    .withPayload(dto)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .setHeader(TYPE_ID_HEADER, CONSUMER_DTO_CLASS.getBytes(StandardCharsets.UTF_8))
                    .build();

            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Métrica publicada en Kafka -> topic={}, partition={}, offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("No se pudo publicar en Kafka (cursoId={}, estudianteId={}): {}",
                            dto.getCursoId(), dto.getEstudianteId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Error al enviar la métrica a Kafka: {}", e.getMessage());
        }
    }
}