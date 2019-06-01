package io.mateu.planing;

import io.mateu.model.Actividad;
import io.mateu.model.Alumno;
import io.mateu.model.Franja;
import io.mateu.model.Nivel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter@Setter
public class CeldaPlaning {

    private final Franja franja;
    private final Alumno alumno;
    private final Actividad actividad;
    private final Ver ver;
    private final LocalDate fecha;
    private final int plazas;
    private final int asistentes;
    private final Nivel nivel;

    public CeldaPlaning(Franja franja, Alumno alumno, Actividad actividad, Nivel nivel, Ver ver, LocalDate fecha, int plazas, int asistentes) {
        this.franja = franja;
        this.alumno = alumno;
        this.actividad = actividad;
        this.nivel = nivel;
        this.ver = ver;
        this.fecha = fecha;
        this.plazas = plazas;
        this.asistentes = asistentes;
    }

    @Override
    public String toString() {
        return (plazas != 0 || asistentes != 0)?"" + asistentes + " / " + plazas:null;
    }
}
