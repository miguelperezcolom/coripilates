package io.mateu;

import com.vaadin.data.provider.QuerySortOrder;
import io.mateu.mdd.core.annotations.Balance;
import io.mateu.mdd.core.annotations.Money;
import io.mateu.mdd.core.interfaces.AbstractJPQLListView;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class ResumenPagosView extends AbstractJPQLListView<ResumenPagosView.Fila> {

    @Override
    public Query buildQuery(EntityManager em, List<QuerySortOrder> sortOrders, boolean forCount) throws Throwable {
        String sql = "select to_char(creado, 'yyyy-MM'), sum(case when importe < 0 then importe else 0 end), sum(case when importe > 0 then importe else 0 end), sum(importe) from pago group by to_char(creado, 'yyyy-MM') order by 1 desc";
        if (forCount) sql = "select count(*) from (" + sql + ") z";
        Query q = em.createNativeQuery(sql);
        return q;
    }

    @Getter@Setter
    public class Fila {
        private String mes;
        @Money
        private double cuotas;
        @Money
        private double cobrado;
        @Money@Balance
        private double saldo;
    }


}
