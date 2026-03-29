package com.accierta.betting.infrastructure.bets;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.port.out.BetCounter;
import org.springframework.stereotype.Component;

@Component
public class NoBetsCounter implements BetCounter {

    @Override
    public int countBets(PorraId porraId) {
        return 0;
    }

    @Override
    public boolean hasMemberBet(PorraId porraId, com.accierta.identity.domain.model.UserId memberId) {
        return false;
    }
}
