package io.mateu.model;

import io.mateu.mdd.core.model.authentication.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity@Getter@Setter
public class UsuarioAlumno extends User {

    @ManyToOne@NotNull
    private Alumno alumno;

}
