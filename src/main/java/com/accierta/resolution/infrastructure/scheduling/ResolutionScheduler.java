package com.accierta.resolution.infrastructure.scheduling;

import com.accierta.resolution.domain.model.ResolutionStatus;
import com.accierta.resolution.domain.port.in.AutoAcceptResolutionCommand;
import com.accierta.resolution.domain.port.in.AutoAcceptResolutionUseCase;
import com.accierta.resolution.domain.port.in.TallyVotesCommand;
import com.accierta.resolution.domain.port.in.TallyVotesUseCase;
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
public class ResolutionScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final AutoAcceptResolutionUseCase autoAcceptResolutionUseCase;
    private final TallyVotesUseCase tallyVotesUseCase;
    private final Clock clock;
    private final boolean enabled;

    public ResolutionScheduler(
            JdbcTemplate jdbcTemplate,
            AutoAcceptResolutionUseCase autoAcceptResolutionUseCase,
            TallyVotesUseCase tallyVotesUseCase,
            Clock clock,
            @Value("${accierta.resolution.auto-accept.enabled:true}") boolean enabled) {
        this.jdbcTemplate = jdbcTemplate;
        this.autoAcceptResolutionUseCase = autoAcceptResolutionUseCase;
        this.tallyVotesUseCase = tallyVotesUseCase;
        this.clock = clock;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${accierta.resolution.auto-accept.fixed-delay-ms:10000}")
    public void autoAcceptWaitingResolutions() {
        if (!enabled) {
            return;
        }
        Instant now = Instant.now(clock);
        List<UUID> resolutionIds = jdbcTemplate.query(
                """
                select resolution_id
                from resolution_projection
                where status = ? and waiting_ends_at <= ?
                order by waiting_ends_at asc
                """,
                (rs, rowNum) -> rs.getObject("resolution_id", UUID.class),
                ResolutionStatus.WAITING.name(),
                Timestamp.from(now));
        resolutionIds.forEach(id -> autoAcceptResolutionUseCase.autoAccept(new AutoAcceptResolutionCommand(id)));
        List<UUID> votingResolutionIds = jdbcTemplate.query(
                """
                select resolution_id
                from resolution_projection
                where status = ? and voting_ends_at <= ?
                order by voting_ends_at asc
                """,
                (rs, rowNum) -> rs.getObject("resolution_id", UUID.class),
                ResolutionStatus.VOTING.name(),
                Timestamp.from(now));
        votingResolutionIds.forEach(id -> tallyVotesUseCase.tally(new TallyVotesCommand(id)));
    }
}
