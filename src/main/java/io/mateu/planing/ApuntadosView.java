package io.mateu.planing;

import com.vaadin.data.provider.QuerySortOrder;
import io.mateu.mdd.core.annotations.Output;
import io.mateu.mdd.core.interfaces.RpcView;
import io.mateu.mdd.core.util.Helper;
import io.mateu.model.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter@Setter
public class ApuntadosView implements RpcView<ApuntadosView, Apuntado> {

    @Output
    private final LocalDate inicio;
    @Output
    private final LocalDate fin;
    @Output
    private final Alumno alumno;
    @Output
    private final Actividad actividad;
    @Output
    private final boolean matricula;
    @Output
    private final Franja franja;

    public ApuntadosView(CeldaPlaning celda) {
        this.inicio = celda.getFecha();
        this.fin = celda.getFecha();
        this.alumno = celda.getAlumno();
        this.actividad = celda.getActividad();
        this.matricula = celda.isMatricula();
        this.franja = celda.getFranja();
    }

    public ApuntadosView(LineaPlaning linea) {
        this.inicio = linea.getLunes().getFecha();
        this.fin = linea.getViernes().getFecha();
        this.alumno = linea.getLunes().getAlumno();
        this.actividad = linea.getLunes().getActividad();
        this.matricula = linea.getLunes().isMatricula();
        this.franja = linea.getLunes().getFranja();
    }

    @Override
    public List<Apuntado> rpc(ApuntadosView filters, List<QuerySortOrder> sortOrders, int offset, int limit) throws Throwable {
        List<Apuntado> l = new ArrayList<>();

        Helper.notransact(em -> {
            List<Asistencia> asistencias = em.createQuery("select x from " + Asistencia.class.getName() + " x").getResultList();
            List<Alumno> alumnos = em.createQuery("select x from " + Alumno.class.getName() + " x where x.activo = true").getResultList();

            if (!filters.isMatricula()) {
                for (Asistencia a : asistencias) {
                    if ((!inicio.isAfter(a.getClase().getFecha()) && !fin.isBefore(a.getClase().getFecha())) && franja.equals(a.getClase().getClase().getSlot().getFranja())) {
                        if (filters.getActividad() == null || filters.getActividad().equals(a.getClase().getClase().getActividad())) {
                            if (filters.getAlumno() == null || filters.getAlumno().equals(a.getAlumno())) l.add(new Apuntado(a));
                        }
                    }
                }
            } else {
                for (Alumno a : alumnos) if (filters.getAlumno() == null || filters.getAlumno().equals(a)) {
                    for (Clase c : a.getMatricula()) if (incluido(c.getSlot().getDia(), inicio, fin)) {
                        if (filters.getActividad() == null || filters.getActividad().equals(c.getActividad())) {
                            if (franja.equals(c.getSlot().getFranja())) l.add(new Apuntado(a, c));
                        }
                    }
                }
            }

        });

        return l;
    }

    private boolean incluido(DiaSemana dia, LocalDate inicio, LocalDate fin) {
        int x;
        switch (dia) {
            case LUNES: x = 0; break;
            case MARTES: x = 1; break;
            case MIERCOLES: x = 2; break;
            case JUEVES: x = 3; break;
            case VIERNES: x = 4; break;
            default: x = -1;
        }

        int a = getNum(inicio.getDayOfWeek());
        int b = getNum(fin.getDayOfWeek());

        return x >= a && x <= b;
    }

    private int getNum(DayOfWeek d) {
        int x;
        switch (d) {
            case MONDAY: x = 0; break;
            case TUESDAY: x = 1; break;
            case WEDNESDAY: x = 2; break;
            case THURSDAY: x = 3; break;
            case FRIDAY: x = 4; break;
            default: x = 1000;
        }
        return x;
    }

    @Override
    public int gatherCount(ApuntadosView filters) throws Throwable {
        return rpc(filters, null, 0, 100).size();
    }


    @Override
    public String toString() {
        return (alumno != null?"" + alumno.getNombre() + " ":"Apuntados ") + inicio.format(DateTimeFormatter.ofPattern("dd MMM")) + (!inicio.equals(fin)?" a " + fin.format(DateTimeFormatter.ofPattern("dd MMM")):"")
                + (franja != null?" " + franja:"")
                + (actividad != null?" " + actividad:"")
                ;
    }

    @Override
    public boolean isEditHandled() {
        return true;
    }

    @Override
    public Object onEdit(Apuntado row) {
        return row.getAsistencia() != null?row.getAsistencia():row.getAlumno();
    }
}
