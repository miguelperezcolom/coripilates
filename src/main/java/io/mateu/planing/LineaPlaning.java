package io.mateu.planing;

import io.mateu.mdd.core.annotations.CellStyleGenerator;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class LineaPlaning {

    private String franja;

    private CeldaPlaning lunes;

    private CeldaPlaning martes;

    private CeldaPlaning miercoles;

    private CeldaPlaning jueves;

    private CeldaPlaning viernes;

}
