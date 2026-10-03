package com.plataforma_lc.asistencia.dto;

import java.util.Date;

public class JustificativoEventDTO {

    private Long id;
    private Long estudianteId;
    private Date fecha;
    private String estado;

    public JustificativoEventDTO() {
    }

    public JustificativoEventDTO(Long id, Long estudianteId, Date fecha, String estado) {
        this.id = id;
        this.estudianteId = estudianteId;
        this.fecha = fecha;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}