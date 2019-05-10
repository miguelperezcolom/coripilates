package io.mateu;

import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.model.config.AppConfig;
import io.mateu.mdd.core.util.Helper;
import io.mateu.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class ConfigSubmenu {

    @Action(order = 0)
    public Object app() throws Throwable {
        return Helper.find(AppConfig.class, 1l);
    }

    @Action(order = 1)
    public Class festivos() {
        return Festivo.class;
    }

    @Action(order = 2)
    public Class vacaciones() {
        return Vacaciones.class;
    }

    @Action(order = 3)
    public Class actividades() {
        return Actividad.class;
    }

    @Action(order = 4)
    public Class franjas() {
        return Franja.class;
    }

    @Action(order = 5)
    public Class slots() {
        return Slot.class;
    }


    @Action(order = 6)
    public Class clases() {
        return Clase.class;
    }


    @Action(order = 20)
    public Class administradores() {
        return UsuarioAdministrador.class;
    }

    @Action(order = 30)
    public void crearAsistencias() throws Throwable {
        Helper.transact(em -> {

            ((List<Asistencia>)em.createQuery("select x from " + Asistencia.class.getName() + " x").getResultList()).forEach(a -> {
                a.getAlumno().getAsistencias().remove(a);
                a.getClase().getAsistencias().remove(a);
                em.remove(a);
            });

            ((List<Asistencia>)em.createQuery("select x from " + ClaseFecha.class.getName() + " x").getResultList()).forEach(a -> {
                em.remove(a);
            });

            ((List<Clase>)em.createQuery("select x from " + Clase.class.getName() + " x").getResultList()).forEach(c -> {
                LocalDate hoy = LocalDate.now();
                LocalDate d = LocalDate.of(hoy.getYear(), hoy.getMonth(), 1);
                while (d.getMonth().equals(hoy.getMonth())) {
                    boolean diaOk = false;
                    if (DiaSemana.LUNES.equals(c.getSlot().getDia()) && DayOfWeek.MONDAY.equals(d.getDayOfWeek())) diaOk = true;
                    if (DiaSemana.MARTES.equals(c.getSlot().getDia()) && DayOfWeek.TUESDAY.equals(d.getDayOfWeek())) diaOk = true;
                    if (DiaSemana.MIERCOLES.equals(c.getSlot().getDia()) && DayOfWeek.WEDNESDAY.equals(d.getDayOfWeek())) diaOk = true;
                    if (DiaSemana.JUEVES.equals(c.getSlot().getDia()) && DayOfWeek.THURSDAY.equals(d.getDayOfWeek())) diaOk = true;
                    if (DiaSemana.VIERNES.equals(c.getSlot().getDia()) && DayOfWeek.FRIDAY.equals(d.getDayOfWeek())) diaOk = true;
                    if (diaOk) {
                        ClaseFecha f = new ClaseFecha();
                        f.setClase(c);
                        f.setFecha(d);
                        em.persist(f);


                        ((List<Alumno>)em.createQuery("select x from " + Alumno.class.getName() + " x").getResultList()).forEach(a -> {
                            if (a.isActivo()) a.getMatricula().forEach(cl -> {

                                if (cl.equals(c)) {
                                    Asistencia s = new Asistencia();
                                    s.setActiva(true);
                                    s.setAlumno(a);
                                    a.getAsistencias().add(s);
                                    s.setClase(f);
                                    s.setClaseOriginal(f);
                                    f.getAsistencias().add(s);
                                    s.setHistorial("");
                                    em.persist(s);
                                }

                            });
                        });


                    }
                    d = d.plusDays(1);
                }

            });



        });
    }

}
