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
public class Festivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;


    @NotEmpty
    private String nombre;

    @NotNull
    LocalDate fecha;



    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof Actividad && id == ((Actividad) obj).getId());
    }

    @Override
    public String toString() {
        return fecha != null && nombre != null?nombre + " " + fecha:getClass().getSimpleName() + " " + id;
    }

}
