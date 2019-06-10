package io.mateu.planing;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.annotations.Width;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.vaadinport.vaadin.MDDUI;
import io.mateu.model.*;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter@Setter
public class NuevaClaseForm {

    @NotNull
    private Alumno alumno;

    @NotNull@Width("600px")
    private ClaseFecha clase;


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

    @Action(order = 1)
    public void grabar() throws Throwable {
        Helper.transact(em -> {
            Asistencia a = new Asistencia();

            a.setAlumno(alumno);
            alumno.getAsistencias().add(a);
            a.setClase(clase);
            clase.getAsistencias().add(a);
            a.setClaseOriginal(clase);

            em.persist(a);
        });

        MDDUI.get().getNavegador().goBack();
    }
}
