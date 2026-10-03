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
            List<Asistencia> asistencias = asistenciaRepository.buscarPorEstudiante(dto.getEstudianteId());
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

            for (Asistencia asistencia : asistencias) {
                if (asistencia.getFecha() != null && dto.getFecha() != null) {
                    // Formatear ambas fechas a 'YYYY-MM-DD' para ignorar diferencias de hora/timestamp
                    String fechaAsistencia = fmt.format(asistencia.getFecha());
                    String fechaDTO = fmt.format(dto.getFecha());

                    if (fechaAsistencia.equals(fechaDTO)) {
                        asistencia.setEstado("JUSTIFIED"); // Ajusta a "JUSTIFIED" o "JUSTIFICADA" según tu estándar
                        asistenciaRepository.save(asistencia);
                        log.info("Asistencia ID {} actualizada exitosamente a JUSTIFIED", asistencia.getId());
                    }
                }
            }
        }
    }
}