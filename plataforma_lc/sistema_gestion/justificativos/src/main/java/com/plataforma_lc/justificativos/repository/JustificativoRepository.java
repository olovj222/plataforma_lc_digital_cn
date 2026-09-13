package com.plataforma_lc.justificativos.repository;

import com.plataforma_lc.justificativos.entities.Justificativo;
import com.plataforma_lc.justificativos.entities.EstadoJustificativo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JustificativoRepository extends JpaRepository<Justificativo, Long> {
    List<Justificativo> findByEstudianteId(Long estudianteId);
    List<Justificativo> findByCursoId(Long cursoId);
    List<Justificativo> findByEstado(EstadoJustificativo estado);
}