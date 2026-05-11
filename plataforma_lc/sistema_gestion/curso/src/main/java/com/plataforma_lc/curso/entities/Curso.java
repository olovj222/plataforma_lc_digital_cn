/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.curso.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del curso es obligatorio")
    private String nombre; // Ej: "1° Básico A"

    @NotNull(message = "El Codigo es obligatorio")
    private Integer codigo;

    // ID del profesor jefe (referencia al MS de usuarios/profesores, no FK real)
    private Long profesorId;

    @Transient
    private String profesorNombre; // Se llena con WebClient si hay MS de usuarios
}