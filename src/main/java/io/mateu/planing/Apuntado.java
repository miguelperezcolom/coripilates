package io.mateu.planing;

import io.mateu.mdd.core.annotations.Balance;
import io.mateu.mdd.core.annotations.Ignored;
import io.mateu.model.*;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class Apuntado {

    @Ignored
    private Alumno alumno;

    @Ignored
    private Asistencia asistencia;

    private String nombre;

    private DiaSemana dia;

    private Actividad actividad;

    private boolean cambio;

    private String telefono;

    @Balance
    private double saldo;


    public Apuntado(Alumno a, Clase c) {
        this.nombre = a.getNombre();
        this.telefono = a.getTelefono();
        this.saldo = a.getSaldo();
        this.alumno = a;
        this.dia = c.getSlot().getDia();
        this.actividad = c.getActividad();
    }

    public Apuntado(Asistencia a) {
        this.nombre = a.getAlumno().getNombre();
        this.telefono = a.getAlumno().getTelefono();
        this.cambio = !a.getClase().equals(a.getClaseOriginal());
        this.saldo = a.getAlumno().getSaldo();
        this.asistencia = a;
        this.dia = a.getClase().getClase().getSlot().getDia();
        this.actividad = a.getClase().getClase().getActividad();
    }
}
