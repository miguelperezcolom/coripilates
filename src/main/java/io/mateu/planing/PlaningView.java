package io.mateu.planing;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.ButtonRenderer;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.interfaces.RpcView;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.vaadinport.vaadin.MDDUI;
import io.mateu.model.*;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter@Setter
public class PlaningView implements RpcView<PlaningView, LineaPlaning> {

    @MainSearchFilter@Width("200px")
    private Alumno alumno;

    @MainSearchFilter@Width("150px")
    private Actividad actividad;

    @MainSearchFilter@Width("150px")
    private Nivel nivel;


    @MainSearchFilter@NotNull
    private Ver ver = Ver.Real;

    @Ignored
    private LocalDate inicio;

    @Ignored
    private LocalDate fin;


    public PlaningView() {
        inicio = LocalDate.now();
        while (!DayOfWeek.MONDAY.equals(inicio.getDayOfWeek())) inicio = inicio.minusDays(1);
        fin = inicio.plusDays(7);
    }

    @Override
    public List<LineaPlaning> rpc(PlaningView filters, List<QuerySortOrder> sortOrders, int offset, int limit) throws Throwable {
        List<LineaPlaning> r = new ArrayList<>();
        Helper.notransact(em -> {

            List<Clase> clases = em.createQuery("select x from " + Clase.class.getName() + " x").getResultList();
            List<Asistencia> asistencias = em.createQuery("select x from " + Asistencia.class.getName() + " x").getResultList();
            List<Alumno> alumnos = em.createQuery("select x from " + Alumno.class.getName() + " x where x.activo = true").getResultList();

            List<Festivo> festivos = em.createQuery("select x from " + Festivo.class.getName() + " x").getResultList();
            List<Vacaciones> vacaciones = em.createQuery("select x from " + Vacaciones.class.getName() + " x").getResultList();

            for (Franja f : (List<Franja>) em.createQuery("select x from " + Franja.class.getName() + " x order by x.desde").getResultList()) {
                LineaPlaning l;
                r.add(l = new LineaPlaning());
                l.setFranja(f.toString());

                LocalDate d = filters.getInicio();

                l.setLunes(getCelda(filters, f, d, DiaSemana.LUNES, clases, asistencias, alumnos, festivos, vacaciones));

                d = d.plusDays(1);

                l.setMartes(getCelda(filters, f, d, DiaSemana.MARTES, clases, asistencias, alumnos, festivos, vacaciones));

                d = d.plusDays(1);

                l.setMiercoles(getCelda(filters, f, d, DiaSemana.MIERCOLES, clases, asistencias, alumnos, festivos, vacaciones));

                d = d.plusDays(1);

                l.setJueves(getCelda(filters, f, d, DiaSemana.JUEVES, clases, asistencias, alumnos, festivos, vacaciones));

                d = d.plusDays(1);

                l.setViernes(getCelda(filters, f, d, DiaSemana.VIERNES, clases, asistencias, alumnos, festivos, vacaciones));

            }
        });
        return r;
    }

    private CeldaPlaning getCelda(PlaningView filters, Franja f, LocalDate d, DiaSemana diaSemana, List<Clase> clases, List<Asistencia> asistencias, List<Alumno> alumnos, List<Festivo> festivos, List<Vacaciones> vacaciones) {
        int plazas = 0;
        int asistentes = 0;

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
            for (Clase c : clases) {
                if (diaSemana.equals(c.getSlot().getDia()) && f.equals(c.getSlot().getFranja())) {
                    if (filters.getActividad() == null || filters.getActividad().equals(c.getActividad())) {
                        if (filters.getNivel() == null || filters.getNivel().equals(c.getNivel())) {
                            if (filters.getAlumno() == null) plazas += c.getCapacidad();
                        }
                    }
                }
            }

            if (Ver.Real.equals(filters.getVer())) {
                for (Asistencia a : asistencias) if (a.isActiva()) {
                    if (d.equals(a.getClase().getFecha()) && f.equals(a.getClase().getClase().getSlot().getFranja())) {
                        if (filters.getActividad() == null || filters.getActividad().equals(a.getClase().getClase().getActividad())) {
                            if (filters.getNivel() == null || filters.getNivel().equals(a.getClase().getClase().getNivel())) {
                                if (filters.getAlumno() == null || filters.getAlumno().equals(a.getAlumno()))
                                    asistentes++;
                            }
                        }
                    }
                }
            } else {
                for (Alumno a : alumnos) if (filters.getAlumno() == null || filters.getAlumno().equals(a)) {
                    for (Clase c : a.getMatricula()) if (diaSemana.equals(c.getSlot().getDia())) {
                        if (filters.getActividad() == null || filters.getActividad().equals(c.getActividad())) {
                            if (filters.getNivel() == null || filters.getNivel().equals(c.getNivel())) {
                                if (f.equals(c.getSlot().getFranja())) asistentes++;
                            }
                        }
                    }
                }
            }
        }

        return new CeldaPlaning(f, filters.getAlumno(), filters.getActividad(), nivel, filters.getVer(), d, plazas, asistentes);
    }

    @Override
    public int gatherCount(PlaningView filters) throws Throwable {
        return ((Long) Helper.nativeSelectValue("select count(*) from franja")).intValue();
    }

    @Override
    public String toString() {
        return "Planing " + (inicio != null?inicio.format(DateTimeFormatter.ofPattern("dd MMM")):"--") + " a " +  (fin != null?fin.format(DateTimeFormatter.ofPattern("dd MMM")):"--");
    }

    @Action(icon = VaadinIcons.ARROW_LEFT, order = 1)
    public void anterior() {
        inicio = inicio.minusDays(7);
        fin = inicio.plusDays(7);
        MDD.updateTitle(toString());
        search();
    }

    @Action(icon = VaadinIcons.ARROW_RIGHT, order = 2)
    public void siguiente() {
        inicio = inicio.plusDays(7);
        fin = inicio.plusDays(7);
        MDD.updateTitle(toString());
        search();
    }

    @Action(icon = VaadinIcons.PLUS, order = 3)
    public NuevaClaseForm añadirClaseSuelta() {
        return new NuevaClaseForm();
    }

    @Action(icon = VaadinIcons.USER, order = 4)
    public PlaningDetallado detallar() {
        return new PlaningDetallado(this);
    }

    @Override
    public void buildColumns(Grid<LineaPlaning> grid) {

        Grid.Column col = grid.addColumn(d -> d.getFranja());
        col.setWidth(170);
        col.setCaption("Franja");

        CeldaPlaningCellStyleGenerator sg = new CeldaPlaningCellStyleGenerator();

        col = grid.addColumn(d -> d.getLunes().toString(), new ButtonRenderer(e -> {
            LineaPlaning i = (LineaPlaning) e.getItem();
            MDDUI.get().getNavegador().go(getState(i.getLunes()));
        }));
        col.setStyleGenerator(v -> sg.getStyles(v, ((LineaPlaning)v).getLunes()));
        col.setWidth(90);
        col.setCaption("Lunes");

        col = grid.addColumn(d -> d.getMartes().toString(), new ButtonRenderer(e -> {
            LineaPlaning i = (LineaPlaning) e.getItem();
            MDDUI.get().getNavegador().go(getState(i.getMartes()));
        }));
        col.setStyleGenerator(v -> sg.getStyles(v, ((LineaPlaning)v).getMartes()));
        col.setWidth(90);
        col.setCaption("Martes");

        col = grid.addColumn(d -> d.getMiercoles().toString(), new ButtonRenderer(e -> {
            LineaPlaning i = (LineaPlaning) e.getItem();
            MDDUI.get().getNavegador().go(getState(i.getMiercoles()));
        }));
        col.setStyleGenerator(v -> sg.getStyles(v, ((LineaPlaning)v).getMiercoles()));
        col.setWidth(90);
        col.setCaption("Miércoles");

        col = grid.addColumn(d -> d.getJueves().toString(), new ButtonRenderer(e -> {
            LineaPlaning i = (LineaPlaning) e.getItem();
            MDDUI.get().getNavegador().go(getState(i.getJueves()));
        }));
        col.setStyleGenerator(v -> sg.getStyles(v, ((LineaPlaning)v).getJueves()));
        col.setWidth(90);
        col.setCaption("Jueves");

        col = grid.addColumn(d -> d.getViernes().toString(), new ButtonRenderer(e -> {
            LineaPlaning i = (LineaPlaning) e.getItem();
            MDDUI.get().getNavegador().go(getState(i.getViernes()));
        }));
        col.setStyleGenerator(v -> sg.getStyles(v, ((LineaPlaning)v).getViernes()));
        col.setWidth(90);
        col.setCaption("Viernes");

    }

    private String getState(CeldaPlaning c) {
        return "" + c.getFecha() + "_" + (c.getActividad() != null?c.getActividad().getId():"n") + "_" + (c.getAlumno() != null?c.getAlumno().getId():"n") + "_" + (c.getFranja() != null?c.getFranja().getId():"n") + "_" + (Ver.Matrícula.equals(c.getVer())?"t":"f") + "_" + (c.getNivel() != null?c.getNivel().getId():"n");
    }


    @Override
    public boolean isEditHandled() {
        return true;
    }

    @Override
    public Object onEdit(LineaPlaning row) {
        return new ApuntadosView(row);
    }

    public Object onEdit(String step) {
        try {
            LocalDate fecha = LocalDate.parse(step.split("_")[0]);
            Actividad actividad = "n".equals(step.split("_")[1])?null:Helper.find(Actividad.class, Long.parseLong(step.split("_")[1]));
            Alumno alumno = "n".equals(step.split("_")[2])?null:Helper.find(Alumno.class, Long.parseLong(step.split("_")[2]));
            Franja franja = Helper.find(Franja.class, Long.parseLong(step.split("_")[3]));
            Ver ver = "t".equals(step.split("_")[4])?Ver.Matrícula:Ver.Real;
            Nivel nivel = "n".equals(step.split("_")[1])?null:Helper.find(Nivel.class, Long.parseLong(step.split("_")[1]));
            CeldaPlaning r = new CeldaPlaning(franja, alumno, actividad, nivel, ver, fecha, 0, 0);
            return new ApuntadosView(r);
        } catch (Throwable e) {
            MDD.alert(e);
        }
        return null;
    }

}
