/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.repository;

import com.plataforma_lc.asistencia.entities.Clase;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaseRepository extends JpaRepository<Clase, Long> {

    @Query("SELECT c FROM Clase c WHERE c.cursoId = :cursoId")
    List<Clase> buscarPorCurso(@Param("cursoId") Long cursoId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Clase c WHERE c.cursoId = :cursoId AND c.fecha = :fecha")
    boolean existeClaseEnFecha(
        @Param("cursoId") Long cursoId,
        @Param("fecha") Date fecha
    );
}