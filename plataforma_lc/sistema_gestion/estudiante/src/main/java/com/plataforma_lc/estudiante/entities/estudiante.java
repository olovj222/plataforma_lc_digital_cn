
package com.plataforma_lc.estudiante.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    private String apPaterno;
    private String apMaterno;
    private String direccion;
    private String telefono;


    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude 
    private List<EstudianteCurso> cursos;

    @ElementCollection
    @CollectionTable(name = "estudiante_asistencia", joinColumns = @JoinColumn(name = "estudiante_id"))
    @Column(name = "registro_asistencia")
    private List<String> asistencias;

    @ElementCollection
    @CollectionTable(name = "estudiante_evaluacion", joinColumns = @JoinColumn(name = "estudiante_id"))
    @Column(name = "nota_evaluacion")
    private List<String> evaluaciones;

}
