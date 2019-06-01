package io.mateu.model;

import com.google.common.collect.Lists;
import com.vaadin.icons.VaadinIcons;
import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.annotations.Ignored;
import io.mateu.mdd.core.annotations.Order;
import io.mateu.mdd.core.annotations.UseCheckboxes;
import io.mateu.mdd.core.util.Helper;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;


    @ManyToOne@NotNull
    private Actividad actividad;

    @ManyToOne@NotNull
    private Slot slot;

    @ManyToOne@NotNull
    private Nivel nivel;

    private int capacidad;


    @Ignored@Column(name = "_order")@Order
    private long order;


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof Clase && id == ((Clase) obj).getId());
    }

    @Override
    public String toString() {
        return actividad != null && nivel != null && slot != null?"" + actividad + " (" + nivel + ") -" + slot:getClass().getSimpleName() + " " + id;
    }

    @Action(order = 1, icon = VaadinIcons.PLUS_CIRCLE_O)
    public static void crearClaseLuVi(@NotNull Franja franja, @NotNull Actividad actividad, @NotNull Nivel nivel, int capacidad, boolean crearSlots) throws Throwable {
        Helper.transact(em -> {
            Lists.newArrayList(DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES).forEach(d -> {
                Slot s = null;
                for (Slot x : (List<Slot>) em.createQuery("select x from " + Slot.class.getName() + " x").getResultList()) {
                    if (d.equals(x.getDia()) && franja.equals(x.getFranja())) {
                        s = x;
                    }
                }
                if (s == null && crearSlots) {
                    s = new Slot();
                    s.setFranja(franja);
                    s.setDia(d);
                    em.persist(s);
                }
                if (s != null) {
                    Clase c = new Clase();
                    c.setSlot(s);
                    c.setActividad(actividad);
                    c.setNivel(nivel);
                    c.setCapacidad(capacidad);
                    em.persist(c);
                }
            });
        });
    }

    @PrePersist
    public void pre() {
        order = Long.parseLong("" + slot.getDia().ordinal() + "" + slot.getFranja().getDesde().format(DateTimeFormatter.ofPattern("HHmm")));
    }

}
