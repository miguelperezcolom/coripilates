package io.mateu.model;

import io.mateu.mdd.core.annotations.Ignored;
import io.mateu.mdd.core.annotations.Output;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;

    private String nombre;

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
        return nombre != null?nombre:getClass().getSimpleName() + " " + id;
    }
}
