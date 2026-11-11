package com.plataforma_lc.asistencia.listener;

import com.plataforma_lc.asistencia.dto.AnaliticaStreamDTO;
import com.plataforma_lc.asistencia.dto.JustificativoEventDTO;
import com.plataforma_lc.asistencia.entities.Asistencia;
import com.plataforma_lc.asistencia.entities.Clase;
import com.plataforma_lc.asistencia.publisher.AnaliticaStreamPublisher;
import com.plataforma_lc.asistencia.repository.AsistenciaRepository;
import com.plataforma_lc.asistencia.repository.ClaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

@Component
public class JustificativoEventListener {

    private static final Logger log = LoggerFactory.getLogger(JustificativoEventListener.class);
    private static final String MARCADOR_FALLO = "SIMULAR_FALLO_ASISTENCIA";

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private AnaliticaStreamPublisher analiticaStreamPublisher;

    @Value("${rabbitmq.simulacion.fallos.habilitada:false}")
    private boolean simulacionFallosHabilitada;

    @RabbitListener(queues = "${rabbitmq.queue.asistencia}")
    public void procesarJustificativoAprobado(JustificativoEventDTO dto) {
        try {
            log.info("Evento recibido desde RabbitMQ para estudiante ID: {} con estado: {}",
                    dto.getEstudianteId(), dto.getEstado());

            if (simulacionFallosHabilitada
                    && dto.getMotivo() != null
                    && dto.getMotivo().contains(MARCADOR_FALLO)) {
                throw new IllegalStateException("Fallo simulado del consumer");
            }

            if ("APROBADO".equalsIgnoreCase(dto.getEstado())) {
                if (dto.getEstudianteId() == null || dto.getFecha() == null) {
                    log.warn("Evento APROBADO omitido por datos incompletos en el DTO: {}", dto);
                    return;
                }

                List<Asistencia> asistencias = asistenciaRepository.buscarPorEstudiante(dto.getEstudianteId());

                // Forzar TimeZone en UTC para evitar desfases de fechas por zona horaria local
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

                String fechaDTO = fmt.format(dto.getFecha());

                boolean actualizada = false;
                for (Asistencia asistencia : asistencias) {
                    if (asistencia.getFecha() != null) {
                        String fechaAsistencia = fmt.format(asistencia.getFecha());

                        if (fechaAsistencia.equals(fechaDTO)) {
                            asistencia.setEstado("JUSTIFIED");
                            asistenciaRepository.save(asistencia);
                            log.info("Asistencia ID {} actualizada exitosamente a JUSTIFIED para la fecha {}",
                                    asistencia.getId(), fechaDTO);
                            actualizada = true;

                            // MySQL ya quedó actualizado: publicar la métrica al stream de analítica (Kafka)
                            publicarMetricaKafka(asistencia, fechaDTO);
                        }
                    }
                }

                if (!actualizada) {
                    log.warn("No se encontró ningún registro de asistencia coincidente para el estudiante ID {} en la fecha {}",
                            dto.getEstudianteId(), fechaDTO);
                }
            } else {
                log.info("Evento procesado e ignorado por estar en estado: {}", dto.getEstado());
            }

        } catch (Exception e) {
            log.error("Error al procesar evento de justificativo: {}. Redirigiendo a DLQ.", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    /**
     * Publica en Kafka la métrica de la asistencia recién actualizada.
     * Nunca lanza excepciones: la actualización en MySQL ya se confirmó y un fallo de
     * Kafka no debe enviar el mensaje de RabbitMQ a la DLQ.
     */
    private void publicarMetricaKafka(Asistencia asistencia, String fechaIso) {
        try {
            Long cursoId = claseRepository.findById(asistencia.getId_clase())
                    .map(Clase::getCursoId)
                    .orElse(null);

            if (cursoId == null) {
                log.warn("Métrica no publicada en Kafka: la asistencia {} no tiene curso asociado (clase {})",
                        asistencia.getId(), asistencia.getId_clase());
                return;
            }

            analiticaStreamPublisher.publicar(AnaliticaStreamDTO.builder()
                    .cursoId(cursoId)
                    .estudianteId(asistencia.getId_estudiante())
                    .fecha(LocalDate.parse(fechaIso))
                    .estado(asistencia.getEstado())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("No se pudo preparar la métrica de Kafka para la asistencia {}: {}",
                    asistencia.getId(), e.getMessage());
        }
    }
}