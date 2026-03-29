package com.accierta.betting.domain.port.out;

import com.accierta.betting.domain.model.PorraId;

public interface BetCounter {

    int countBets(PorraId porraId);

    boolean hasMemberBet(PorraId porraId, com.accierta.identity.domain.model.UserId memberId);
}
