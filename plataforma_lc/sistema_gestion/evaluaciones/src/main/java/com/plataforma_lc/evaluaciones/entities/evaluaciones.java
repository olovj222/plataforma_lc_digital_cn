/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.evaluaciones.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
/**
 *
 * @author juako
 */
@Data
@Entity
public class evaluaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la evaluación es obligatorio")
    private String nombre;

    @NotNull(message = "El curso es obligatorio")
    private Long cursoId;  // ← cambia de String curso a Long cursoId

    @NotNull(message = "El estudiante es obligatorio")
    private Long estudianteId;

    private int calificacion;

    @Transient
    private String cursoNombre;  // ← nuevo, no se guarda en BD
}