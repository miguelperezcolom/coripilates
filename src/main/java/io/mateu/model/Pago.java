package io.mateu.model;

import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.core.workflow.WorkflowEngine;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity@Getter@Setter@Indelible@Unmodifiable
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Ignored
    private int version;

    @Output
    private LocalDateTime creado = LocalDateTime.now();

    @ManyToOne@NotNull
    private Alumno alumno;

    @NotNull
    private FormaPago formaPago;

    private boolean ajusteCuota;

    private String concepto;

    @Money@Balance
    private double importe;

    @PostPersist@PostUpdate
    public void post() {
        WorkflowEngine.add(() -> {
            try {
                Helper.transact(em -> {

                    em.find(Alumno.class, getAlumno().getId()).setUpdateRqTime(LocalDateTime.now());

                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && id > 0 && obj instanceof Pago && id == ((Pago) obj).getId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + id;
    }
}
