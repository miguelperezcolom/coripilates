package io.mateu.planing;

import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.FullWidth;
import io.mateu.mdd.core.annotations.Html;
import io.mateu.mdd.core.annotations.Ignored;
import io.mateu.mdd.core.annotations.Output;
import io.mateu.mdd.core.util.Helper;
import io.mateu.model.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter@Setter
public class PlaningDetallado {

    @Ignored
    private final PlaningView filters;

    @FullWidth@Output
    private String planing;

    public PlaningDetallado(PlaningView filters) {
        this.filters = filters;

        final String[] h = {"<table border='1' width='1000px'>"};

        try {
            Helper.notransact(em -> {

                List<Clase> clases = em.createQuery("select x from " + Clase.class.getName() + " x").getResultList();
                List<Asistencia> asistencias = em.createQuery("select x from " + Asistencia.class.getName() + " x").getResultList();
                List<Alumno> alumnos = em.createQuery("select x from " + Alumno.class.getName() + " x where x.activo = true").getResultList();

                List<Festivo> festivos = em.createQuery("select x from " + Festivo.class.getName() + " x").getResultList();
                List<Vacaciones> vacaciones = em.createQuery("select x from " + Vacaciones.class.getName() + " x").getResultList();

                for (Franja f : (List<Franja>) em.createQuery("select x from " + Franja.class.getName() + " x order by x.desde").getResultList()) {
                    String tr = "<tr><td  style='vertical-align: top;'>" + f + "</td>";

                    LocalDate d = filters.getInicio();

                    tr += getCelda(filters, f, d, DiaSemana.LUNES, clases, asistencias, alumnos, festivos, vacaciones);

                    d = d.plusDays(1);

                    tr += getCelda(filters, f, d, DiaSemana.MARTES, clases, asistencias, alumnos, festivos, vacaciones);

                    d = d.plusDays(1);

                    tr += getCelda(filters, f, d, DiaSemana.MIERCOLES, clases, asistencias, alumnos, festivos, vacaciones);

                    d = d.plusDays(1);

                    tr += getCelda(filters, f, d, DiaSemana.JUEVES, clases, asistencias, alumnos, festivos, vacaciones);

                    d = d.plusDays(1);

                    tr += getCelda(filters, f, d, DiaSemana.VIERNES, clases, asistencias, alumnos, festivos, vacaciones);


                    h[0] += tr;
                }
            });
        } catch (Throwable throwable) {
            MDD.alert(throwable);
        }


        h[0] += "</table>";

        planing = h[0];

    }


    private String getCelda(PlaningView filters, Franja f, LocalDate d, DiaSemana diaSemana, List<Clase> clases, List<Asistencia> asistencias, List<Alumno> alumnos, List<Festivo> festivos, List<Vacaciones> vacaciones) {
        int plazas = 0;
        int asistentes = 0;

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

        String h = "<td style='vertical-align: top;'>";

        if (laborable) {
            for (Clase c : clases) {
                if (diaSemana.equals(c.getSlot().getDia()) && f.equals(c.getSlot().getFranja())) {
                    if (filters.getActividad() == null || filters.getActividad().equals(c.getActividad())) {
                        if (filters.getNivel() == null || filters.getNivel().equals(c.getNivel())) {
                            if (filters.getAlumno() == null) plazas += c.getCapacidad();
                        }
                    }
                }
            }

            if (Ver.Real.equals(filters.getVer())) {
                for (Asistencia a : asistencias) if (a.isActiva()) {
                    if (d.equals(a.getClase().getFecha()) && f.equals(a.getClase().getClase().getSlot().getFranja())) {
                        if (filters.getActividad() == null || filters.getActividad().equals(a.getClase().getClase().getActividad())) {
                            if (filters.getNivel() == null || filters.getNivel().equals(a.getClase().getClase().getNivel())) {
                                if (filters.getAlumno() == null || filters.getAlumno().equals(a.getAlumno())) {
                                    asistentes++;
                                    h += a.getAlumno() + "<br/>";
                                }
                            }
                        }
                    }
                }
            } else {
                for (Alumno a : alumnos) if (filters.getAlumno() == null || filters.getAlumno().equals(a)) {
                    for (Clase c : a.getMatricula()) if (diaSemana.equals(c.getSlot().getDia())) {
                        if (filters.getActividad() == null || filters.getActividad().equals(c.getActividad())) {
                            if (filters.getNivel() == null || filters.getNivel().equals(c.getNivel())) {
                                if (f.equals(c.getSlot().getFranja())) {
                                    asistentes++;
                                    h += a + "<br/>";
                                }
                            }
                        }
                    }
                }
            }
            h += "" + asistentes + "/" + plazas + "<br/>";
        } else {
            h += "---";
        }

        h += "</td>";

        return h;
    }

    @Override
    public String toString() {
        return "Planing detalle " + (filters.getInicio() != null?filters.getInicio().format(DateTimeFormatter.ofPattern("dd MMM")):"--") + " a " +  (filters.getFin() != null?filters.getFin().format(DateTimeFormatter.ofPattern("dd MMM")):"--");
    }

}
