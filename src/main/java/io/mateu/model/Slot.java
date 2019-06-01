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
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Setter
public class Slot implements Comparable<Slot> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;

    @NotNull
    @ListColumn@Order(priority = 20)
    private DiaSemana dia;

    @NotNull
    @ManyToOne
    @ListColumn@Order(priority = 40)
    private Franja franja;

    @Ignored@Column(name = "_order")@Order
    private long order;

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
        return dia != null && franja != null?dia.name() + " " + franja:getClass().getSimpleName() + " " + id;
    }


    @Action(order = 1, icon = VaadinIcons.PLUS_CIRCLE_O)
    public static void crearSlotsLuVi(@NotNull Franja franja) throws Throwable {
        Helper.transact(em -> {
            Lists.newArrayList(DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES).forEach(d -> {
                Slot s = new Slot();
                s.setFranja(franja);
                s.setDia(d);
                em.persist(s);
            });
        });
    }

    @Override
    public int compareTo(@org.jetbrains.annotations.NotNull Slot slot) {
        return dia.compareTo(slot.dia);
    }

    @PrePersist
    public void pre() {
        order = Long.parseLong("" + dia.ordinal() + "" + franja.getDesde().format(DateTimeFormatter.ofPattern("HHmm")));
    }

}
