package io.mateu.model;

import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.core.workflow.WorkflowEngine;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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

    private String nombre;

    private boolean activo = true;

    private double cuota;

    @KPI@Balance
    private double saldo;

    @OneToMany@UseChips
    private Set<Clase> matricula = new HashSet<>();

    private String email;

    private String telefono;

    @TextArea
    private String comentarios;


    @OneToMany(mappedBy = "alumno")@Ignored
    private List<Asistencia> asistencias = new ArrayList<>();


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
        return nombre != null?nombre:getClass().getSimpleName() + " " + id;
    }


    @PostPersist@PostUpdate
    public void post() {
        WorkflowEngine.add(() -> {
            try {
                Helper.transact(em -> {

                    em.find(Alumno.class, getId()).actualizarSaldo();

                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }


}
