package io.mateu.planing;

import io.mateu.model.Actividad;
import io.mateu.model.Alumno;
import io.mateu.model.Franja;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter@Setter
public class CeldaPlaning {

    private final Franja franja;
    private final Alumno alumno;
    private final Actividad actividad;
    private final boolean matricula;
    private final LocalDate fecha;
    private final int plazas;
    private final int asistentes;

    public CeldaPlaning(Franja franja, Alumno alumno, Actividad actividad, boolean matricula, LocalDate fecha, int plazas, int asistentes) {
        this.franja = franja;
        this.alumno = alumno;
        this.actividad = actividad;
        this.matricula = matricula;
        this.fecha = fecha;
        this.plazas = plazas;
        this.asistentes = asistentes;
    }

    @Override
    public String toString() {
        return (plazas != 0 || asistentes != 0)?"" + asistentes + " / " + plazas:null;
    }
}
