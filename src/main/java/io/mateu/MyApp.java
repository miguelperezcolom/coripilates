package io.mateu;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.annotations.SubApp;
import io.mateu.mdd.core.app.*;
import io.mateu.mdd.core.interfaces.RpcView;
import io.mateu.model.*;
import io.mateu.planing.PlaningView;

import java.util.ArrayList;
import java.util.List;

public class MyApp extends SimpleMDDApplication {

    @SubApp(order = 1, icon = VaadinIcons.COG)
    public ConfigSubmenu config() {
        return new ConfigSubmenu();
    }


    @Action(order = 3, icon = VaadinIcons.GROUP)
    public Class alumnos() {
        return Alumno.class;
    }


    @Action(order = 4, icon = VaadinIcons.CALENDAR)
    public RpcView planing() {
        return new PlaningView();
    }
    /*
    @Action(order = 4, icon = VaadinIcons.CALENDAR)
    public Component planing() {
        Image i = new Image(null, new ExternalResource("https://thumbs.dreamstime.com/z/color-rosado-rojo-de-la-raya-semanal-del-planificador-55291594.jpg"));
        return i;
    }
    */

    @Action(order = 6, icon = VaadinIcons.TABLE)
    public Class asistencias() {
        return Asistencia.class;
    }

    @Action(order = 7, icon = VaadinIcons.EURO)
    public Class pagos() {
        return Pago.class;
    }

    @Action(order = 8, icon = VaadinIcons.CHART)
    public ResumenPagosView resumen() {
        return new ResumenPagosView();
    }

    @Action(order = 9, icon = VaadinIcons.CHECK)
    public Check check() {
        return new Check();
    }

    /*
    @Action(order = 30, icon = VaadinIcons.USER)
    public Class usuarios() {
        return UsuarioAlumno.class;
    }
    */



    @Override
    public boolean isAuthenticationNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "CoriPilates";
    }

    @Override
    public AbstractAction getDefaultAction() {
        return new MDDOpenCustomComponentAction("Resumen", Home.class);
    }
}