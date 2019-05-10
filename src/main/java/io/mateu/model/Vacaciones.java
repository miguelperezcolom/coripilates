package io.mateu.model;

import io.mateu.mdd.core.annotations.Ignored;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Vacaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;


    @NotEmpty
    private String nombre;

    @NotNull
    LocalDate inicio;

    @NotNull
    LocalDate fin;


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof Vacaciones && id == ((Vacaciones) obj).getId());
    }

    @Override
    public String toString() {
        return inicio != null && fin != null && nombre != null?nombre + " " + inicio + " " + fin:getClass().getSimpleName() + " " + id;
    }

}
