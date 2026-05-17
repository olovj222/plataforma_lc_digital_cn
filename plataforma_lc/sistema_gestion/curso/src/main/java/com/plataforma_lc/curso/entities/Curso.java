/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.curso.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class Curso {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "Matemáticas")
    @NotBlank(message = "El nombre del curso es obligatorio")
    private String nombre;

    @Schema(example = "101")
    @NotNull(message = "El Codigo es obligatorio")
    private Integer codigo;

    @Schema(example = "1")
    private Long profesorId;

    @Schema(hidden = true)
    @Transient
    private String profesorNombre;
}