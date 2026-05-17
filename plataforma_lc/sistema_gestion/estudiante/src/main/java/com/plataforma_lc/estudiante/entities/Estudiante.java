
package com.plataforma_lc.estudiante.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author Olov
 */

@Data
@Entity
public class Estudiante {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Schema(example = "Juan")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Schema(example = "Pérez")
    private String apPaterno;

    @Schema(example = "López")
    private String apMaterno;

    @Schema(example = "Av. Principal 123")
    private String direccion;

    @Schema(example = "912345678")
    private String telefono;

    @Schema(description = "Lista de cursos asignados al estudiante")
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    private List<EstudianteCurso> cursos;

    @Schema(hidden = true)
    @ElementCollection
    @CollectionTable(name = "estudiante_asistencia", joinColumns = @JoinColumn(name = "estudiante_id"))
    @Column(name = "registro_asistencia")
    private List<String> asistencias;

    @Schema(hidden = true)
    @ElementCollection
    @CollectionTable(name = "estudiante_evaluacion", joinColumns = @JoinColumn(name = "estudiante_id"))
    @Column(name = "nota_evaluacion")
    private List<String> evaluaciones;
}
