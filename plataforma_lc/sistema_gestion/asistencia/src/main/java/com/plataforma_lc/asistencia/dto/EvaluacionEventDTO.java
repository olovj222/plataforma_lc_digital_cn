package com.plataforma_lc.asistencia.dto;

/**
 * DTO para deserializar el evento de evaluación publicado por el MS evaluaciones.
 * El consumer (asistencia) declara solo los campos que necesita del payload JSON.
 */
public class EvaluacionEventDTO {

    private Long id;
    private Long estudianteId;
    private Long cursoId;
    private int calificacion;
    private String nombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }

    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
