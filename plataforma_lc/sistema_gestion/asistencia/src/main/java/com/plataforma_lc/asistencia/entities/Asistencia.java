/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.plataforma_lc.asistencia.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.Data;

@Data
@Entity
public class Asistencia {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    private long id_curso;
    private long id_estudiante;
    private String estado;
    @Temporal(TemporalType.DATE)
    private Date fecha;

}
