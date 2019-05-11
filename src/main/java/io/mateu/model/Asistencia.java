package io.mateu.model;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.core.workflow.WorkflowEngine;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    public DataProvider getClaseDataProvider() {
        List<ClaseFecha> l = new ArrayList<>();

        try {
            Helper.notransact(em -> {

                List<ClaseFecha> todas = em.createQuery("select x from " + ClaseFecha.class.getName() + " x where x.fecha >= :h").setParameter("h", LocalDate.now()).getResultList();
                for (ClaseFecha x : todas) {
                    if (x.getAsistencias().size() < x.getClase().getCapacidad()) {
                        l.add(x);
                    }
                }

                l.sort(Comparator.comparing(ClaseFecha::getFecha).thenComparing(a -> a.getClase().getSlot().getFranja().getDesde()));

            });
        } catch (Throwable throwable) {
            MDD.alert(throwable);
        }

        return new ListDataProvider(l);
    }

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
