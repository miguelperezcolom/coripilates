package io.mateu.model;

import io.mateu.mdd.core.annotations.Ignored;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Planing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof Planing && id == ((Planing) obj).getId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + id;
    }

}
