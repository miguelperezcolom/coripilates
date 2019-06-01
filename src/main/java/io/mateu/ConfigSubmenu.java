package io.mateu;

import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.model.config.AppConfig;
import io.mateu.mdd.core.util.Helper;
import io.mateu.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConfigSubmenu {

    @Action(order = 0)
    public Object app() throws Throwable {
        return Helper.find(AppConfig.class, 1l);
    }

    @Action(order = 10)
    public Class festivos() {
        return Festivo.class;
    }

    @Action(order = 20)
    public Class vacaciones() {
        return Vacaciones.class;
    }

    @Action(order = 30)
    public Class niveles() {
        return Nivel.class;
    }

    @Action(order = 35)
    public Class actividades() {
        return Actividad.class;
    }

    @Action(order = 40)
    public Class franjas() {
        return Franja.class;
    }

    @Action(order = 50)
    public Class slots() {
        return Slot.class;
    }


    @Action(order = 60)
    public Class clases() {
        return Clase.class;
    }


    @Action(order = 70)
    public Class administradores() {
        return UsuarioAdministrador.class;
    }

    @Action(order = 80)
    public void crearCargos() throws Throwable {
        Helper.transact(em -> {
            ((List<Alumno>)em.createQuery("select x from " + Alumno.class.getName() + " x").getResultList()).forEach(a -> {
                if (a.isActivo()) {
                    Pago p = new Pago();
                    p.setAlumno(a);
                    p.setConcepto("Cuota " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yy")));
                    p.setImporte(Helper.roundEuros(-1d * a.getCuota()));
                    em.persist(p);
                }
            });
        });
    }

    @Action(order = 90)
    public void ordenar() throws Throwable {
        Helper.transact(em -> {
            ((List<Slot>)em.createQuery("select x from " + Slot.class.getName() + " x").getResultList()).forEach(a -> {
                a.pre();
            });
            ((List<Clase>)em.createQuery("select x from " + Clase.class.getName() + " x").getResultList()).forEach(a -> {
                a.pre();
            });
            ((List<ClaseFecha>)em.createQuery("select x from " + ClaseFecha.class.getName() + " x").getResultList()).forEach(a -> {
                a.pre();
            });
            ((List<Asistencia>)em.createQuery("select x from " + Asistencia.class.getName() + " x").getResultList()).forEach(a -> {
                a.pre();
            });
        });
    }

    @Action(order = 90)
    public void crearAsistencias() throws Throwable {
        Helper.transact(em -> {

            ((List<Asistencia>)em.createQuery("select x from " + Asistencia.class.getName() + " x").getResultList()).forEach(a -> {
                a.getAlumno().getAsistencias().remove(a);
                a.getClase().getAsistencias().remove(a);
                em.remove(a);
            });

            ((List<ClaseFecha>)em.createQuery("select x from " + ClaseFecha.class.getName() + " x").getResultList()).forEach(a -> {
                em.remove(a);
            });

            List<Festivo> festivos = em.createQuery("select x from " + Festivo.class.getName() + " x").getResultList();
            List<Vacaciones> vacaciones = em.createQuery("select x from " + Vacaciones.class.getName() + " x").getResultList();


            ((List<Clase>)em.createQuery("select x from " + Clase.class.getName() + " x").getResultList()).forEach(c -> {

                List<ClaseFecha> clasesFecha = em.createQuery("select x from " + ClaseFecha.class.getName() + " x where x.clase = :c").setParameter("c", c).getResultList();


                LocalDate hoy = LocalDate.now();
                LocalDate d = hoy.plusDays(0);LocalDate.of(hoy.getYear(), hoy.getMonth(), 1);
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


                            LocalDate finalD = d;
                            ((List<Alumno>)em.createQuery("select x from " + Alumno.class.getName() + " x").getResultList()).forEach(a -> {
                                if (a.isActivo() && (a.getAntiguedad() == null || finalD.isAfter(a.getAntiguedad()))) a.getMatricula().forEach(cl -> {

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

                    }

                    d = d.plusDays(1);
                }

            });



        });
    }

}
