package io.mateu.model;

import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.core.workflow.WorkflowEngine;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity@Getter@Setter@NewNotAllowed@Indelible
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;

    @Output@NotInList
    private LocalDateTime creado = LocalDateTime.now();

    @ManyToOne@NotNull@Output@SearchFilter
    private Alumno alumno;

    @ManyToOne@NotNull@Output@SearchFilter
    private ClaseFecha claseOriginal;


    @SearchFilter
    private boolean activa = true;

    @ManyToOne@NotNull
    @SearchFilter
    private ClaseFecha clase;

    @Output
    private LocalDateTime ultimoCambio;


    @Output@NotInList
    private String historial;


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof Asistencia && id == ((Asistencia) obj).getId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + id;
    }

}
