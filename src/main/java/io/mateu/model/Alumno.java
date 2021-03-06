package io.mateu.model;

import com.google.common.base.Strings;
import com.vaadin.icons.VaadinIcons;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.model.util.EmailHelper;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.core.workflow.WorkflowEngine;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity@Getter@Setter
public class Alumno {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;

    @Ignored
    private LocalDateTime updateRqTime = LocalDateTime.now();

    @Output@NotInList
    private LocalDateTime creado = LocalDateTime.now();

    @NotInList
    private LocalDate antiguedad;

    @MainSearchFilter
    private String nombre;

    @ColumnWidth(80)
    private boolean activo = true;

    public void setActivo(boolean activo) {
        this.activo = activo;
        setUpdateRqTime(LocalDateTime.now());
    }

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
    private String password;

    @Ignored
    private String passwordResetKey;

    private boolean appHabilitada;

    @Output
    private LocalDateTime ultimoAcceso;

    @Ignored
    private boolean preActivo;
    @Ignored
    private transient Set<Clase> preMatricula = new HashSet<>();

    public void setMatricula(Set<Clase> matricula) {
        this.matricula = matricula;
        setUpdateRqTime(LocalDateTime.now());
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
        return (nombre != null?nombre + " (" + (nivel != null && !Strings.isNullOrEmpty(nivel.getNombre())?nivel.getNombre().substring(0, 1):"-") + ")":getClass().getSimpleName() + " " + id) + (activo?"":"X");
    }

    @PostLoad
    public void postLoad() {
        preMatricula.addAll(matricula);
        preActivo = activo;
    }

    @PostPersist@PostUpdate
    public void post() {
        if (updateRqTime != null) WorkflowEngine.add(() -> {
            try {
                Helper.transact(em -> {

                    Alumno a = em.find(Alumno.class, getId());

                    if (a.getUpdateRqTime() != null) {

                        try {
                            a.actualizarSaldo();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        try {
                            a.actualizarAsistencias(em, preMatricula, preActivo);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        a.setUpdateRqTime(null);
                    }

                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Action(saveAfter = true, icon = VaadinIcons.WARNING)
    public void resetAsistencias(EntityManager em) {
        new ArrayList<>(asistencias).forEach(a -> {
            a.getClase().getAsistencias().remove(a);
            em.remove(a);
            asistencias.remove(a);
        });
        actualizarAsistencias(em, new HashSet<>(), isActivo());
    }

    @Action
    public static void actualizarSaldos() throws Throwable {
        Helper.transact(em -> {
            em.createQuery("select x from " + Alumno.class.getName() + " x order by x.id", Alumno.class).getResultList().forEach(a -> a.setUpdateRqTime(LocalDateTime.now()));
        });
    }

    @Action(order = 200, icon = VaadinIcons.WARNING)
    public static void resetTodasAsistencias() throws Throwable {
        Helper.transact(em -> {
            em.createQuery("select x from " + Alumno.class.getName() + " x order by x.id", Alumno.class).getResultList().forEach(a ->
                            //        a.setUpdateRqTime(LocalDateTime.now())
                    {
                        new ArrayList<>(a.getAsistencias()).stream().filter(as -> as.isActiva() && as.getUltimoCambio() == null).forEach(as -> {
                            as.getClase().getAsistencias().remove(as);
                            em.remove(as);
                            a.getAsistencias().remove(as);
                        });
                        a.actualizarAsistencias(em, new HashSet<>(), a.isActivo());

                    }
            );
        });
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


    public String getAppId() {
        return Base64.getEncoder().encodeToString(("" + id).getBytes());
    }

    @Action
    public static void enviarEmailAccesoApp(EntityManager em, Set<Alumno> seleccion) throws Throwable {
        for (Alumno a : seleccion) {
            a.enviarEmailAccesoApp();
        }
    }

    @Action(order = 10, saveAfter = true)
    public void enviarEmailAccesoApp() throws Throwable {
        setAppHabilitada(true);
        EmailHelper.sendEmail(getEmail(), "Acceso a CoriPilates", "<p>Estimado " + getNombre() + ",</p>" +
                "<p>puedes acceder a nuestra app en " + System.getProperty("urlapp", "http://localhost:8080") + "/" + getAppId() + "/"  +".</p>" +
                "<pGracias por confiar en nosotros,</p>" +
                "<p>CoriPilates</p>", false);
    }

    @Action(order = 20, saveAfter = true)
    public void resetPassword() throws Throwable {
        setPassword(null);
    }

    @Action(order = 30, saveAfter = true)
    public void enviarEmailResetPasswordApp() throws Throwable {
        setPasswordResetKey(Base64.getEncoder().encodeToString(LocalDateTime.now().toString().getBytes()));
        EmailHelper.sendEmail(getEmail(), "CoriPilates password reset", "<p>Estimado " + getNombre() + ",</p>" +
                "<p>Puedes resetear tu password en " + System.getProperty("urlapp", "http://localhost:8080") + "/" + getAppId() + "/reset/" + getPasswordResetKey()  +".</p>" +
                "<pGracias por confiar en nosotros,</p>" +
                "<p>CoriPilates</p>", false);
    }
}
