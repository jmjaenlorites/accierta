package com.accierta.settlement.domain.port.out;

import com.accierta.settlement.domain.model.Debt;
import com.accierta.settlement.domain.model.DebtId;
import java.util.Optional;

public interface DebtRepository {

    void save(Debt debt);

    Optional<Debt> findById(DebtId debtId);
}
