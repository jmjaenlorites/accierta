package com.accierta.betting.infrastructure.lifecycle;

import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.port.in.CloseBettingCommand;
import com.accierta.betting.domain.port.in.CloseBettingUseCase;
import com.accierta.betting.domain.port.in.EnableResolutionCommand;
import com.accierta.betting.domain.port.in.EnableResolutionUseCase;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PorraLifecycleScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final CloseBettingUseCase closeBettingUseCase;
    private final EnableResolutionUseCase enableResolutionUseCase;
    private final Clock clock;
    private final boolean enabled;

    public PorraLifecycleScheduler(
            JdbcTemplate jdbcTemplate,
            CloseBettingUseCase closeBettingUseCase,
            EnableResolutionUseCase enableResolutionUseCase,
            Clock clock,
            @Value("${accierta.betting.lifecycle.enabled:true}") boolean enabled) {
        this.jdbcTemplate = jdbcTemplate;
        this.closeBettingUseCase = closeBettingUseCase;
        this.enableResolutionUseCase = enableResolutionUseCase;
        this.clock = clock;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${accierta.betting.lifecycle.fixed-delay-ms:10000}")
    public void processDeadlines() {
        if (!enabled) {
            return;
        }
        Instant now = Instant.now(clock);
        processOpenPorras(now);
        processClosedPorras(now);
    }

    private void processOpenPorras(Instant now) {
        List<UUID> porraIds = jdbcTemplate.query(
                """
                select porra_id
                from porra_projection
                where status = ? and betting_deadline <= ?
                order by betting_deadline asc
                """,
                (rs, rowNum) -> rs.getObject("porra_id", UUID.class),
                PorraStatus.OPEN.name(),
                Timestamp.from(now));
        porraIds.forEach(porraId -> closeBettingUseCase.close(new CloseBettingCommand(porraId)));
    }

    private void processClosedPorras(Instant now) {
        List<UUID> porraIds = jdbcTemplate.query(
                """
                select porra_id
                from porra_projection
                where status = ? and resolution_date <= ?
                order by resolution_date asc
                """,
                (rs, rowNum) -> rs.getObject("porra_id", UUID.class),
                PorraStatus.CLOSED.name(),
                Timestamp.from(now));
        porraIds.forEach(porraId -> enableResolutionUseCase.enable(new EnableResolutionCommand(porraId)));
    }
}
