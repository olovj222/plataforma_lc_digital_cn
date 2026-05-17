/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.evaluaciones.entities;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class Evaluaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(example = "Evaluación 1")
    @NotBlank(message = "El nombre de la evaluación es obligatorio")
    private String nombre;
    
    @Schema(example = "1")
    @NotNull(message = "El curso es obligatorio")
    private Long cursoId;

    @Schema(example = "1")
    @NotNull(message = "El estudiante es obligatorio")
    private Long estudianteId;
    
    @Schema(example = "0")
    private int calificacion;
    
    @Schema(hidden = true)
    @Transient
    private String cursoNombre;
    
     @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Transient
    private String estudianteNombre; 
}