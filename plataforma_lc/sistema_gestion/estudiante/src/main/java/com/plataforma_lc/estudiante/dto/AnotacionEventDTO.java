package com.plataforma_lc.estudiante.dto;

/**
 * DTO ligero para deserializar eventos de anotación NEGATIVA
 * publicados por el MS anotaciones vía RabbitMQ.
 *
 * Intencionalmente desacoplado de la entidad Anotacion del MS anotaciones:
 * el MS estudiante no debe depender de clases de otro microservicio.
 */
public class AnotacionEventDTO {

    private Long id;
    private Long estudianteId;
    private Long cursoId;
    private String tipo;         // "NEGATIVA"
    private String descripcion;
    private String fecha;        // ISO "YYYY-MM-DD"
    private String autorId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getAutorId() { return autorId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }
}
