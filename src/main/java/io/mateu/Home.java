package io.mateu;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import io.mateu.mdd.core.CSS;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.util.Helper;
import io.mateu.model.Alumno;
import io.mateu.model.Pago;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class Home extends VerticalLayout {

    @Override
    public String toString() {
        return "Home";
    }

    public Home() {

        addStyleName(CSS.NOPADDING);

        try {
            List<Alumno> alumnos = Helper.selectObjects("select x from " + Alumno.class.getName() + " x");

            addComponent(new Label("" + alumnos.stream().filter(a -> a.isActivo()).count() + " alumnos activos"));

            addComponent(new Label("" + alumnos.stream().filter(a -> a.getSaldo() < 0).count() + " alumnos con saldos pendientes"));

            addComponent(new Label("" + new DecimalFormat("##,###,###,###,##0.00").format(alumnos.stream().filter(a -> a.getSaldo() < 0).map(a -> -1d * a.getSaldo()).reduce((t, v) -> t + v).orElse(0.0)) + " euros pendiente cobro"));

            addComponent(new Label("" + alumnos.stream().filter(a -> a.isActivo() && a.getSaldo() < 0).count() + " alumnos pendientes de pago"));

            double pendienteCobro = alumnos.stream().filter(a -> a.isActivo() && a.getSaldo() < 0).map(a -> -1d * a.getSaldo()).reduce((t, v) -> t + v).orElse(0.0);
            double totalCuotas = alumnos.stream().filter(a -> a.isActivo()).map(a -> a.getCuota()).reduce((t, v) -> t + v).orElse(0.0);

            addComponent(new Label("" + new DecimalFormat("##,###,###,###,##0.00").format(totalCuotas) + " euros cuotas"));

            addComponent(new Label("" + new DecimalFormat("##,###,###,###,##0.00").format(totalCuotas - pendienteCobro) + " euros cobrados cuotas (" + new DecimalFormat("###########0").format(100d * (totalCuotas - pendienteCobro) / totalCuotas) + "%). Faltan por cobrar: " + new DecimalFormat("##,###,###,###,##0.00").format(pendienteCobro)));

            {
                final String[] h = {"<table><tr><th>Nº clases por semana</th><th style='padding-left: 20px;'>Nº alumnos</th></tr>"};
                alumnos.stream().filter(a -> a.isActivo()).map(a -> a.getMatricula().size()).distinct().sorted().forEach(c -> {
                    h[0] += "<tr><td style='text-align: center;'>" + c + "</td><td style='padding-left: 20px; text-align: right;'>" + alumnos.stream().filter(a -> a.isActivo() && a.getMatricula().size() == c).count() + "</td></tr>";
                });
                h[0] += "</table>";

                addComponent(new Label(h[0], ContentMode.HTML));
            }

            {
                final String[] h = {"<table><tr><th>Forma de pago</th><th style='padding-left: 20px;'>Total cobrado desde día 1</th></tr>"};
                List<Pago> pagos = Helper.selectObjects("select x from " + Pago.class.getName() + " x where x.creado >= :d", Helper.hashmap("d", LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 1, 0, 0)));
                pagos.stream().filter(a -> !a.isAjusteCuota()).map(a -> a.getFormaPago()).distinct().sorted().forEach(c -> {
                    h[0] += "<tr><td style='text-align: left;'>" + c + "</td><td style='padding-left: 20px; text-align: right;'>" +  new DecimalFormat("##,###,###,###,##0.00").format(pagos.stream().filter(a -> (c == null && a.getFormaPago() == null) || c.equals(a.getFormaPago())).map(a -> a.getImporte()).reduce((t, v) -> t + v).orElse(0.0)) + "</td></tr>";
                });
                h[0] += "</table>";

                addComponent(new Label(h[0], ContentMode.HTML));
            }

            addComponentsAndExpand(new Label(""));

        } catch (Throwable throwable) {
            MDD.alert(throwable);
        }

    }
}
