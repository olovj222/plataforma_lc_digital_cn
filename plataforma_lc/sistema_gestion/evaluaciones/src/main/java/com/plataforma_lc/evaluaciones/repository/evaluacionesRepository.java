/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.plataforma_lc.evaluaciones.repository;


import com.plataforma_lc.evaluaciones.entities.evaluaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 *
 * @author juako
 */
public interface evaluacionesRepository extends JpaRepository<evaluaciones, Long> {
    List<evaluaciones> findByCursoId(Long cursoId);
    List<evaluaciones> findByEstudianteId(Long estudianteId);
}