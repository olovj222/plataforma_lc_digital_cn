/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.plataforma_lc.evaluaciones.repository;


import com.plataforma_lc.evaluaciones.entities.Evaluaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author juako
 */
@Repository
public interface evaluacionesRepository extends JpaRepository<Evaluaciones, Long> {
    List<Evaluaciones> findByCursoId(Long cursoId);
    List<Evaluaciones> findByEstudianteId(Long estudianteId);
}