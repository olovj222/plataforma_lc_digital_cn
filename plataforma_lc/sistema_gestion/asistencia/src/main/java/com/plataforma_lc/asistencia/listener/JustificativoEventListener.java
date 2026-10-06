package com.plataforma_lc.asistencia.listener;

import com.plataforma_lc.asistencia.dto.JustificativoEventDTO;
import com.plataforma_lc.asistencia.entities.Asistencia;
import com.plataforma_lc.asistencia.repository.AsistenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

@Component
public class JustificativoEventListener {

    private static final Logger log = LoggerFactory.getLogger(JustificativoEventListener.class);

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @RabbitListener(queues = "${rabbitmq.queue.asistencia}")
    public void procesarJustificativoAprobado(JustificativoEventDTO dto) {
        log.info("Evento recibido desde RabbitMQ para estudiante ID: {} con estado: {}",
                dto.getEstudianteId(), dto.getEstado());

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
                    }
                }
            }

            if (!actualizada) {
                log.warn(
                        "No se encontró ningún registro de asistencia coincidente para el estudiante ID {} en la fecha {}",
                        dto.getEstudianteId(), fechaDTO);
            }
        } else {
            log.info("Evento procesado e ignorado por estar en estado: {}", dto.getEstado());
        }
    }
}