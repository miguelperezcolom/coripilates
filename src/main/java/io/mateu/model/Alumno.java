package io.mateu.model;

import com.google.common.base.Strings;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.core.workflow.WorkflowEngine;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity@Getter@Setter
public class Alumno {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;

    @Ignored
    private LocalDateTime updateRqTime;

    @Output@NotInList
    private LocalDateTime creado = LocalDateTime.now();

    @NotInList
    private LocalDate antiguedad;

    @MainSearchFilter
    private String nombre;

    @ColumnWidth(80)
    private boolean activo = true;

    @ColumnWidth(100)
    private double cuota;

    @KPI@Balance@ColumnWidth(100)
    private double saldo;

    @OneToMany@UseChips
    private Set<Clase> matricula = new HashSet<>();

    @NotNull@ManyToOne@NotInList
    private Nivel nivel;

    @ColumnWidth(200)
    private String email;

    @MainSearchFilter
    @ColumnWidth(120)
    private String telefono;

    @TextArea
    private String comentarios;


    @OneToMany(mappedBy = "alumno")@Ignored
    private List<Asistencia> asistencias = new ArrayList<>();


    @Ignored
    private boolean preActivo;
    @Ignored
    private transient Set<Clase> preMatricula = new HashSet<>();

    public void setMatricula(Set<Clase> matricula) {
        this.matricula = matricula;
    }

    public void actualizarSaldo() {
        setSaldo(0);
        try {
            ((List<Pago>)Helper.selectObjects("select x from " + Pago.class.getName() + " x where x.alumno.id = " + getId())).forEach(p -> setSaldo(getSaldo() + p.getImporte()));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }



    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof Alumno && id == ((Alumno) obj).getId());
    }

    @Override
    public String toString() {
        return nombre != null?nombre + " (" + (nivel != null && !Strings.isNullOrEmpty(nivel.getNombre())?nivel.getNombre().substring(0, 1):"-") + ")":getClass().getSimpleName() + " " + id;
    }

    @PostLoad
    public void postLoad() {
        preMatricula.addAll(matricula);
        preActivo = activo;
    }

    @PostPersist@PostUpdate
    public void post() {
        WorkflowEngine.add(() -> {
            try {
                Helper.transact(em -> {

                    Alumno a = em.find(Alumno.class, getId());

                    a.actualizarSaldo();

                    a.actualizarAsistencias(em, preMatricula, preActivo);

                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Action(saveAfter = true)
    public void resetAsistencias(EntityManager em) {
        new ArrayList<>(asistencias).forEach(a -> {
            a.getClase().getAsistencias().remove(a);
            em.remove(a);
            asistencias.remove(a);
        });
        actualizarAsistencias(em, new HashSet<>(), isActivo());
    }

    private void actualizarAsistencias(EntityManager em, Set<Clase> preMatriculax, boolean preActivox) {

        if (!activo) {
            asistencias.forEach(a -> a.setActiva(false));
        } else {

            List<Festivo> festivos = em.createQuery("select x from " + Festivo.class.getName() + " x").getResultList();
            List<Vacaciones> vacaciones = em.createQuery("select x from " + Vacaciones.class.getName() + " x").getResultList();


            matricula.forEach(c -> {

                if (!preMatriculax.contains(c) || !preActivox) {

                    List<ClaseFecha> clasesFecha = em.createQuery("select x from " + ClaseFecha.class.getName() + " x where x.clase = :c").setParameter("c", c).getResultList();

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
                            if (DiaSemana.LUNES.equals(c.getSlot().getDia()) && DayOfWeek.MONDAY.equals(d.getDayOfWeek())) diaOk = true;
                            if (DiaSemana.MARTES.equals(c.getSlot().getDia()) && DayOfWeek.TUESDAY.equals(d.getDayOfWeek())) diaOk = true;
                            if (DiaSemana.MIERCOLES.equals(c.getSlot().getDia()) && DayOfWeek.WEDNESDAY.equals(d.getDayOfWeek())) diaOk = true;
                            if (DiaSemana.JUEVES.equals(c.getSlot().getDia()) && DayOfWeek.THURSDAY.equals(d.getDayOfWeek())) diaOk = true;
                            if (DiaSemana.VIERNES.equals(c.getSlot().getDia()) && DayOfWeek.FRIDAY.equals(d.getDayOfWeek())) diaOk = true;
                            if (diaOk) {

                                if (antiguedad == null || d.isAfter(antiguedad)) {

                                    ClaseFecha f = null;
                                    for (ClaseFecha x : clasesFecha) {
                                        if (x.getFecha().equals(d)) {
                                            f = x;
                                            break;
                                        }
                                    }

                                    if (f != null) {

                                        Asistencia a = null;
                                        for (Asistencia x : asistencias) {
                                            if (f.equals(x.getClaseOriginal())) {
                                                a = x;
                                                break;
                                            }
                                        }

                                        if (a == null) {
                                            a = new Asistencia();
                                            a.setAlumno(this);
                                            asistencias.add(a);
                                            a.setClase(f);
                                            a.setClaseOriginal(f);
                                            f.getAsistencias().add(a);
                                            a.setHistorial("");
                                            em.persist(a);
                                        }

                                        a.setActiva(activo);
                                    }

                                }

                            }

                        }

                        d = d.plusDays(1);
                    }
                }

            });

            preMatriculax.forEach(c -> {
                if (!matricula.contains(c)) {

                    List<Asistencia> borrar = new ArrayList<>();

                    asistencias.forEach(a -> {
                        if (a.getClase().getClase().equals(c)) borrar.add(a);
                    });

                    asistencias.removeAll(borrar);

                    borrar.forEach(a -> {
                        a.getClase().getAsistencias().remove(a);
                        em.remove(a);
                    });

                }
            });

        }


    }


}
