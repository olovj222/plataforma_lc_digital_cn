package com.plataforma_lc.anotaciones.repository;

import com.plataforma_lc.anotaciones.entities.Anotacion;
import com.plataforma_lc.anotaciones.entities.TipoAnotacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnotacionRepository extends JpaRepository<Anotacion, Long> {
    List<Anotacion> findByEstudianteId(Long estudianteId);
    List<Anotacion> findByCursoId(Long cursoId);
    List<Anotacion> findByEstudianteIdAndTipo(Long estudianteId, TipoAnotacion tipo);
}