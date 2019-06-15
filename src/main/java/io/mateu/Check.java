package io.mateu;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.util.Helper;
import io.mateu.model.*;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class Check extends VerticalLayout {

    private final Panel p;

    @Override
    public String toString() {
        return "Home";
    }

    public Check() {

        addComponent(new Button(VaadinIcons.REFRESH, e -> {
            refrescar();
        }));

            p = new Panel();
            addComponentsAndExpand(p);

        }

    private void refrescar() {
        VerticalLayout vl = new VerticalLayout();

        try {
            List<Alumno> alumnos = Helper.selectObjects("select x from " + Alumno.class.getName() + " x");

            List<Festivo> festivos = Helper.selectObjects("select x from " + Festivo.class.getName() + " x");
            List<Vacaciones> vacaciones = Helper.selectObjects("select x from " + Vacaciones.class.getName() + " x");

            final int[] oks = {0};
            final int[] kos = {0};

            alumnos.forEach(a -> {

                boolean ok = true;

                if (a.isActivo()) {
                    for (Clase c : a.getMatricula()) {

                        List<ClaseFecha> clasesFecha = null;
                        try {
                            clasesFecha = Helper.selectObjects("select x from " + ClaseFecha.class.getName() + " x where x.clase = :c", Helper.hashmap("c", c));

                            LocalDate hoy = LocalDate.now();
                            LocalDate d = hoy.plusDays(0); //LocalDate.of(hoy.getYear(), hoy.getMonth(), 1);
                            while (d.getMonth().equals(hoy.getMonth())) {

                                boolean laborable = true;

                                if (laborable) for (Festivo v : festivos) {
                                    if (d.equals(v.getFecha())) {
                                        laborable = false;
                                        break;
                                    }
                                }

                                if (laborable) for (Vacaciones v : vacaciones) {
                                    if (!d.isBefore(v.getInicio()) && !d.isAfter(v.getFin())) {
                                        laborable = false;
                                        break;
                                    }
                                }

                                if (laborable) {

                                    boolean diaOk = false;
                                    if (DiaSemana.LUNES.equals(c.getSlot().getDia()) && DayOfWeek.MONDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (DiaSemana.MARTES.equals(c.getSlot().getDia()) && DayOfWeek.TUESDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (DiaSemana.MIERCOLES.equals(c.getSlot().getDia()) && DayOfWeek.WEDNESDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (DiaSemana.JUEVES.equals(c.getSlot().getDia()) && DayOfWeek.THURSDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (DiaSemana.VIERNES.equals(c.getSlot().getDia()) && DayOfWeek.FRIDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (diaOk) {

                                        if (a.getAntiguedad() == null || d.isAfter(a.getAntiguedad())) {

                                            ClaseFecha f = null;
                                            for (ClaseFecha x : clasesFecha) {
                                                if (x.getFecha().equals(d)) {
                                                    f = x;
                                                    break;
                                                }
                                            }

                                            if (f != null) {

                                                Asistencia as = null;
                                                for (Asistencia x : a.getAsistencias()) {
                                                    if (f.equals(x.getClaseOriginal())) {
                                                        as = x;
                                                        break;
                                                    }
                                                }

                                                if (as == null) {
                                                    vl.addComponent(new Label("Falta " + a + " " + f));
                                                    ok = false;
                                                }

                                            }

                                        }

                                    }

                                }

                                d = d.plusDays(1);
                            }

                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }

                    }
                } else {

                    for (Clase c : a.getMatricula()) {

                        List<ClaseFecha> clasesFecha = null;
                        try {
                            clasesFecha = Helper.selectObjects("select x from " + ClaseFecha.class.getName() + " x where x.clase = :c", Helper.hashmap("c", c));

                            LocalDate hoy = LocalDate.now();
                            LocalDate d = hoy.plusDays(0); //LocalDate.of(hoy.getYear(), hoy.getMonth(), 1);
                            while (d.getMonth().equals(hoy.getMonth())) {

                                boolean laborable = true;

                                if (laborable) for (Festivo v : festivos) {
                                    if (d.equals(v.getFecha())) {
                                        laborable = false;
                                        break;
                                    }
                                }

                                if (laborable) for (Vacaciones v : vacaciones) {
                                    if (!d.isBefore(v.getInicio()) && !d.isAfter(v.getFin())) {
                                        laborable = false;
                                        break;
                                    }
                                }

                                if (laborable) {

                                    boolean diaOk = false;
                                    if (DiaSemana.LUNES.equals(c.getSlot().getDia()) && DayOfWeek.MONDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (DiaSemana.MARTES.equals(c.getSlot().getDia()) && DayOfWeek.TUESDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (DiaSemana.MIERCOLES.equals(c.getSlot().getDia()) && DayOfWeek.WEDNESDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (DiaSemana.JUEVES.equals(c.getSlot().getDia()) && DayOfWeek.THURSDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (DiaSemana.VIERNES.equals(c.getSlot().getDia()) && DayOfWeek.FRIDAY.equals(d.getDayOfWeek()))
                                        diaOk = true;
                                    if (diaOk) {

                                        if (a.getAntiguedad() == null || d.isAfter(a.getAntiguedad())) {

                                            ClaseFecha f = null;
                                            for (ClaseFecha x : clasesFecha) {
                                                if (x.getFecha().equals(d)) {
                                                    f = x;
                                                    break;
                                                }
                                            }

                                            if (f != null) {

                                                Asistencia as = null;
                                                for (Asistencia x : a.getAsistencias()) {
                                                    if (f.equals(x.getClaseOriginal())) {
                                                        as = x;
                                                        break;
                                                    }
                                                }

                                                if (as != null) {
                                                    vl.addComponent(new Label("Sobra " + a + " " + f));
                                                    ok = false;
                                                }

                                            }

                                        }

                                    }

                                }

                                d = d.plusDays(1);
                            }

                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }

                    }

                }

                if (ok) oks[0]++;
                else kos[0]++;

            });

            vl.addComponent(new Label("" + oks[0] + " alumnos ok"));
            vl.addComponent(new Label("" + kos[0] + " alumnos ko"));

        } catch (Throwable e) {
            MDD.alert(e);
        }

        p.setContent(vl);
    }
}
