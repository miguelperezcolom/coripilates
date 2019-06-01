package io.mateu.planing;

import io.mateu.mdd.core.interfaces.ICellStyleGenerator;

import java.time.LocalDate;

public class CeldaPlaningCellStyleGenerator implements ICellStyleGenerator {
    @Override
    public String getStyles(Object row, Object value) {
        String css = "celdacalendario";

        CeldaPlaning s = (CeldaPlaning) value;

        if (s != null) {

            if (s.getFecha().isBefore(LocalDate.now())) {
                css += "  mdd-lightsteelblue-bgd";
            } else {

                if (s.getPlazas() == 0 && s.getAsistentes() == 0) {
                    css += " cell-void";
                } else {
                    if (s.getPlazas() > s.getAsistentes()) css += " success";
                    else if (s.getPlazas() == s.getAsistentes()) css += " mdd-orange-bgd";
                    else if (s.getPlazas() < s.getAsistentes()) css += " danger";

                /*

                css += " ";
                if (s.getMaxEstadoValidacion() == 0) css += "cell-valid";
                else if (s.getMaxEstadoValidacion() < 2) css += "cell-warning";
                else if (s.getMaxEstadoValidacion() >= 2) css += "cell-invalid";

                */
                }

            }

        }


        return css;
    }

    @Override
    public boolean isContentShown() {
        return true;
    }
}
