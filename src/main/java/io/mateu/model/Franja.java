package io.mateu.model;

import com.google.common.collect.Lists;
import com.vaadin.icons.VaadinIcons;
import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.annotations.Ignored;
import io.mateu.mdd.core.annotations.ListColumn;
import io.mateu.mdd.core.annotations.Order;
import io.mateu.mdd.core.util.Helper;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class Franja implements Comparable<Franja> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;

    @ListColumn@Order
    private LocalTime desde;

    @ListColumn
    private LocalTime hasta;

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof Franja && id == ((Franja) obj).getId());
    }

    @Override
    public String toString() {
        return desde != null && hasta != null?"de " + desde + " a " + hasta:getClass().getSimpleName() + " " + id;
    }

    @Override
    public int compareTo(@org.jetbrains.annotations.NotNull Franja franja) {
        return desde.compareTo(franja.desde);
    }
}
