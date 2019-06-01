package io.mateu.model;

import com.google.common.base.Strings;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.core.workflow.WorkflowEngine;
import io.mateu.planing.NuevaClaseForm;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @ManyToOne@NotNull@Output@SearchFilter@ColumnWidth(450)
    private ClaseFecha claseOriginal;


    @SearchFilter
    private boolean activa = true;

    @ManyToOne@NotNull
    @SearchFilter@ColumnWidth(450)
    private ClaseFecha clase;

    @Ignored
    private transient ClaseFecha clasePre;

    @Ignored
    private transient boolean activaPre;

    @Ignored@Column(name = "_order")@Order
    private long order;

    public DataProvider getClaseDataProvider() {
        List<ClaseFecha> l = new ArrayList<>();

        try {
            Helper.notransact(em -> {

                List<Festivo> festivos = em.createQuery("select x from " + Festivo.class.getName() + " x").getResultList();
                List<Vacaciones> vacaciones = em.createQuery("select x from " + Vacaciones.class.getName() + " x").getResultList();

                List<ClaseFecha> todas = em.createQuery("select x from " + ClaseFecha.class.getName() + " x where x.fecha >= :h").setParameter("h", LocalDate.now()).getResultList();
                for (ClaseFecha x : todas) {
                    boolean laborable = true;

                    if (laborable) for (Festivo v : festivos) {
                        if (x.getFecha().equals(v.getFecha())) {
                            laborable = false;
                            break;
                        }
                    }

                    if (laborable) for (Vacaciones v : vacaciones) {
                        if (!x.getFecha().isBefore(v.getInicio()) && !x.getFecha().isAfter(v.getFin())) {
                            laborable = false;
                            break;
                        }
                    }

                    if (laborable && x.getAsistencias().size() < x.getClase().getCapacidad()) {
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

    @PostLoad
    public void postLoad() {
        clasePre = clase;
        activaPre = activa;
    }

    @PreUpdate
    public void preUpdate() {
        if (!clase.equals(clasePre)) {
            ultimoCambio = LocalDateTime.now();
            if (historial == null) historial = "";
            if (!Strings.isNullOrEmpty(historial)) historial += "<br/>";
            historial += ultimoCambio + " cambió a " + clase;
        }
        if (activa != activaPre) {
            ultimoCambio = LocalDateTime.now();
            if (historial == null) historial = "";
            if (!Strings.isNullOrEmpty(historial)) historial += "<br/>";
            historial += ultimoCambio + " " + (activa?" activada":" desactivada");
        }
    }


    @Action(icon = VaadinIcons.PLUS, order = 3)
    public static NuevaClaseForm añadirClaseSuelta() {
        return new NuevaClaseForm();
    }

    @PrePersist
    public void pre() {
        if (clase != null) order = Long.parseLong(clase.getFecha().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + clase.getClase().getSlot().getFranja().getDesde().format(DateTimeFormatter.ofPattern("HHmm")));
        else if (claseOriginal != null) order = Long.parseLong(claseOriginal.getFecha().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + claseOriginal.getClase().getSlot().getFranja().getDesde().format(DateTimeFormatter.ofPattern("HHmm")));
        else order = 0;
    }

}
