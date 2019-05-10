package io.mateu;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.annotations.SubApp;
import io.mateu.mdd.core.app.SimpleMDDApplication;
import io.mateu.model.Alumno;
import io.mateu.model.Asistencia;
import io.mateu.model.Pago;
import io.mateu.model.UsuarioAlumno;

public class AdminApp extends SimpleMDDApplication {

    @SubApp(order = 1, icon = VaadinIcons.COG)
    public ConfigSubmenu config() {
        return new ConfigSubmenu();
    }


    @Action(order = 3, icon = VaadinIcons.GROUP)
    public Class alumnos() {
        return Alumno.class;
    }


    @Action(order = 4, icon = VaadinIcons.CALENDAR)
    public Component planing() {
        Image i = new Image(null, new ExternalResource("https://thumbs.dreamstime.com/z/color-rosado-rojo-de-la-raya-semanal-del-planificador-55291594.jpg"));
        return i;
    }

    @Action(order = 6, icon = VaadinIcons.TABLE)
    public Class asistencias() {
        return Asistencia.class;
    }

    @Action(order = 7, icon = VaadinIcons.EURO)
    public Class pagos() {
        return Pago.class;
    }

    @Action(order = 30, icon = VaadinIcons.USER)
    public Class usuarios() {
        return UsuarioAlumno.class;
    }

}