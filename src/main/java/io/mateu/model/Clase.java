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
import java.time.DayOfWeek;
import java.time.LocalDate;
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

    @Action
    public void crearClasesParaEsteMes() throws Throwable {
            Helper.transact(em -> {

                List<ClaseFecha> clases = (List<ClaseFecha>) em.createQuery("select x from " + ClaseFecha.class.getName() + " x where x.clase = :c").setParameter("c", this).getResultList();

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


                                ClaseFecha f = null;
                                for (ClaseFecha x : clasesFecha) {
                                    if (x.getFecha().equals(d)) {
                                        f = x;
                                        break;
                                    }
                                }

                                if (f == null) {
                                    f = new ClaseFecha();
                                    f.setClase(c);
                                    f.setFecha(d);
                                    em.persist(f);


                                    LocalDate finalD = d;
                                    ClaseFecha finalF = f;
                                    ((List<Alumno>)em.createQuery("select x from " + Alumno.class.getName() + " x").getResultList()).forEach(a -> {
                                        if (a.isActivo() && (a.getAntiguedad() == null || finalD.isAfter(a.getAntiguedad()))) a.getMatricula().forEach(cl -> {

                                            if (cl.equals(c)) {
                                                Asistencia s = new Asistencia();
                                                s.setActiva(true);
                                                s.setAlumno(a);
                                                a.getAsistencias().add(s);
                                                s.setClase(finalF);
                                                s.setClaseOriginal(finalF);
                                                finalF.getAsistencias().add(s);
                                                s.setHistorial("");
                                                em.persist(s);
                                            }

                                        });
                                    });
                                }

                            }

                        }

                        d = d.plusDays(1);
                    }

                });



            });
        }

}
