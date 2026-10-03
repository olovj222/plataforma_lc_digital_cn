package com.plataforma_lc.justificativos.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "justificativos")
public class Justificativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long estudianteId;

    @Column(nullable = false)
    private Long cursoId;

    @Column(nullable = false)
    private LocalDate fechaInasistencia;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoJustificativo estado = EstadoJustificativo.PENDIENTE;

    @Column(nullable = false)
    private String autorId; // X-User-Id del profesor que lo creó

    private String resueltoPor; // X-User-Id del admin que aprobó/rechazó

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaResolucion;

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }
    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }
    public LocalDate getFechaInasistencia() { return fechaInasistencia; }
    public void setFechaInasistencia(LocalDate fechaInasistencia) { this.fechaInasistencia = fechaInasistencia; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public EstadoJustificativo getEstado() { return estado; }
    public void setEstado(EstadoJustificativo estado) { this.estado = estado; }
    public String getAutorId() { return autorId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }
    public String getResueltoPor() { return resueltoPor; }
    public void setResueltoPor(String resueltoPor) { this.resueltoPor = resueltoPor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}