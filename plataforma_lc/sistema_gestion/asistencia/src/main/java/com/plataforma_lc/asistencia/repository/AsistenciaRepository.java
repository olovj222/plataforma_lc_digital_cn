/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.plataforma_lc.asistencia.repository;

import com.plataforma_lc.asistencia.entities.Asistencia;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface AsistenciaRepository extends JpaRepository<Asistencia, Long>{
    
    @Query("SELECT a FROM Asistencia a JOIN Clase c ON a.id_clase = c.id WHERE c.cursoId = :idCurso")
    List<Asistencia> buscarPorCurso(@Param("idCurso") long idCurso);
    
    @Query("SELECT a FROM Asistencia a WHERE a.id_clase = :idClase")
    List<Asistencia> buscarPorClase(@Param("idClase") long idClase);

    @Query("SELECT a FROM Asistencia a WHERE a.id_estudiante = :idEstudiante")
    List<Asistencia> buscarPorEstudiante(@Param("idEstudiante") long idEstudiante);
    
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Asistencia a WHERE a.id_clase = :idClase AND a.id_estudiante = :idEstudiante AND a.fecha = :fecha")
    boolean existeRegistroDuplicado(
        @Param("idClase") long idClase,
        @Param("idEstudiante") long idEstudiante,
        @Param("fecha") Date fecha
    );
}
