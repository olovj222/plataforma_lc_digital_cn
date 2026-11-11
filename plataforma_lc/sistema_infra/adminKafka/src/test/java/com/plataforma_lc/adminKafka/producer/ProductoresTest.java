package com.plataforma_lc.adminKafka.producer;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.AlertaEventDTO;
import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoresTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private AuditoriaProducer auditoriaProducer;

    @InjectMocks
    private AnaliticaProducer analiticaProducer;

    @InjectMocks
    private AlertaProducer alertaProducer;

    private SendResult<String, Object> mockSendResult(String topic, int partition, long offset) {
        RecordMetadata rm = new RecordMetadata(new TopicPartition(topic, partition), offset, 0, System.currentTimeMillis(), 0, 0);
        return new SendResult<>(null, rm);
    }

    @Test
    @DisplayName("AuditoriaProducer: publica con clave 'servicio' y maneja éxito")
    void auditoriaProducer_publicaExito() {
        AuditoriaEventDTO evento = AuditoriaEventDTO.builder()
                .servicio("estudiante")
                .accion("CREATE")
                .usuario("admin")
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(mockSendResult(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS, 0, 10L));
        when(kafkaTemplate.send(eq(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS), eq("estudiante"), eq(evento)))
                .thenReturn(future);

        assertDoesNotThrow(() -> auditoriaProducer.publicar(evento));
        verify(kafkaTemplate).send(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS, "estudiante", evento);
    }

    @Test
    @DisplayName("AuditoriaProducer: registra error sin lanzar excepción si send falla")
    void auditoriaProducer_fallaSinLanzarExcepcion() {
        AuditoriaEventDTO evento = AuditoriaEventDTO.builder()
                .servicio("estudiante")
                .accion("CREATE")
                .usuario("admin")
                .build();

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Error en broker"));
        when(kafkaTemplate.send(eq(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS), eq("estudiante"), eq(evento)))
                .thenReturn(future);

        assertDoesNotThrow(() -> auditoriaProducer.publicar(evento));
        verify(kafkaTemplate).send(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS, "estudiante", evento);
    }

    @Test
    @DisplayName("AnaliticaProducer: publica con clave 'cursoId'")
    void analiticaProducer_publicaExito() {
        AnaliticaStreamDTO dto = AnaliticaStreamDTO.builder()
                .cursoId(5L)
                .estudianteId(20L)
                .fecha(LocalDate.now())
                .estado("PRESENTE")
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(mockSendResult(KafkaTopics.ASISTENCIA_ANALITICA_STREAM, 1, 5L));
        when(kafkaTemplate.send(eq(KafkaTopics.ASISTENCIA_ANALITICA_STREAM), eq("5"), eq(dto)))
                .thenReturn(future);

        assertDoesNotThrow(() -> analiticaProducer.publicar(dto));
        verify(kafkaTemplate).send(KafkaTopics.ASISTENCIA_ANALITICA_STREAM, "5", dto);
    }

    @Test
    @DisplayName("AlertaProducer: publica con clave 'alertaId'")
    void alertaProducer_publicaExito() {
        AlertaEventDTO evento = AlertaEventDTO.builder()
                .alertaId("alt-123")
                .tipoAlerta("INASISTENCIA")
                .nivel("CRITICAL")
                .mensaje("Alerta de prueba")
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(mockSendResult(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS, 2, 25L));
        when(kafkaTemplate.send(eq(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS), eq("alt-123"), eq(evento)))
                .thenReturn(future);

        assertDoesNotThrow(() -> alertaProducer.publicar(evento));
        verify(kafkaTemplate).send(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS, "alt-123", evento);
    }
}

