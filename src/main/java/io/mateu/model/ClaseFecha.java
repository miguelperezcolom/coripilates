package io.mateu.model;

import io.mateu.mdd.core.annotations.Ignored;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity@Getter@Setter
public class ClaseFecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;


    @ManyToOne@NotNull
    private Clase clase;

    @NotNull
    private LocalDate fecha;


    @OneToMany(mappedBy = "clase")@Ignored
    private List<Asistencia> asistencias = new ArrayList<>();




    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof ClaseFecha && id == ((ClaseFecha) obj).getId());
    }

    @Override
    public String toString() {
        return clase != null && fecha != null?"" + fecha.format(DateTimeFormatter.ofPattern("EEE dd/MMMM")) + " " + clase:getClass().getSimpleName() + " " + id;
    }

}
