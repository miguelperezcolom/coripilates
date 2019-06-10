package io.mateu;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.util.Helper;
import io.mateu.model.Alumno;

import java.text.DecimalFormat;
import java.util.List;

public class Home extends VerticalLayout {

    @Override
    public String toString() {
        return "Home";
    }

    public Home() {

        try {
            List<Alumno> alumnos = Helper.selectObjects("select x from " + Alumno.class.getName() + " x");

            addComponent(new Label("" + alumnos.stream().filter(a -> a.isActivo()).count() + " alumnos activos"));

            addComponent(new Label("" + alumnos.stream().filter(a -> a.getSaldo() < 0).count() + " alumnos con saldos pendientes"));

            addComponent(new Label("" + new DecimalFormat("##,###,###,###,##0.00").format(alumnos.stream().filter(a -> a.getSaldo() < 0).map(a -> -1d * a.getSaldo()).reduce((t, v) -> t + v).orElse(0.0)) + " euros pendiente cobro"));

            addComponent(new Label("" + alumnos.stream().filter(a -> a.isActivo() && a.getSaldo() < 0).count() + " alumnos pendiente pago"));

            addComponent(new Label("" + new DecimalFormat("##,###,###,###,##0.00").format(alumnos.stream().filter(a -> a.isActivo() && a.getSaldo() < 0).map(a -> -1d * a.getSaldo()).reduce((t, v) -> t + v).orElse(0.0)) + " euros pendiente cobro de alumnos activos"));

            addComponentsAndExpand(new Label(""));

        } catch (Throwable throwable) {
            MDD.alert(throwable);
        }

    }
}
