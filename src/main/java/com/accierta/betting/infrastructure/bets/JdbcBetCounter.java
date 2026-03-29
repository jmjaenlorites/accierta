package com.accierta.betting.infrastructure.bets;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.port.out.BetCounter;
import com.accierta.identity.domain.model.UserId;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Primary
public class JdbcBetCounter implements BetCounter {

    private final JdbcTemplate jdbcTemplate;

    public JdbcBetCounter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int countBets(PorraId porraId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from porra_bet_projection where porra_id = ?",
                Integer.class,
                porraId.value());
        return count == null ? 0 : count;
    }

    @Override
    public boolean hasMemberBet(PorraId porraId, UserId memberId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from porra_bet_projection where porra_id = ? and member_id = ?",
                Integer.class,
                porraId.value(),
                memberId.value());
        return count != null && count > 0;
    }
}
