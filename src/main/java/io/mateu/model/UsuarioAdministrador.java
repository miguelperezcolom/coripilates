package io.mateu.model;

import io.mateu.mdd.core.model.authentication.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class UsuarioAdministrador extends User {
}
