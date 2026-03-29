package com.accierta.betting.domain.model;

import com.accierta.betting.domain.event.BetModified;
import com.accierta.betting.domain.event.BetPlaced;
import com.accierta.betting.domain.event.BettingDeadlineReached;
import com.accierta.betting.domain.event.PorraCreated;
import com.accierta.betting.domain.event.PorraAutoCancelled;
import com.accierta.betting.domain.event.PorraDeletedByCreator;
import com.accierta.betting.domain.event.ResolutionEnabled;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.domain.AggregateRoot;
import com.accierta.shared.domain.DomainEvent;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Porra extends AggregateRoot {

    private static final Duration CREATOR_DELETE_WINDOW = Duration.ofMinutes(15);

    private PorraId id;
    private GroupId groupId;
    private UserId creatorId;
    private String title;
    private String description;
    private PorraType type;
    private List<String> options;
    private Double tolerance;
    private int stake;
    private Instant bettingDeadline;
    private Instant resolutionDate;
    private BetVisibility visibility;
    private boolean betModifiable;
    private PorraStatus status;
    private Instant createdAt;
    private final Map<UserId, Bet> bets = new HashMap<>();

    private Porra() {
    }

    public static Porra create(
            PorraId porraId,
            GroupId groupId,
            UserId creatorId,
            String title,
            String description,
            PorraType type,
            List<String> options,
            Double tolerance,
            int stake,
            Instant bettingDeadline,
            Instant resolutionDate,
            BetVisibility visibility,
            Boolean betModifiable,
            Clock clock) {
        Objects.requireNonNull(porraId, "porraId cannot be null");
        Objects.requireNonNull(groupId, "groupId cannot be null");
        Objects.requireNonNull(creatorId, "creatorId cannot be null");
        Objects.requireNonNull(title, "title cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(bettingDeadline, "bettingDeadline cannot be null");
        Objects.requireNonNull(resolutionDate, "resolutionDate cannot be null");
        Objects.requireNonNull(visibility, "visibility cannot be null");
        Objects.requireNonNull(clock, "clock cannot be null");

        String normalizedTitle = title.trim();
        if (normalizedTitle.isEmpty()) {
            throw new IllegalArgumentException("title cannot be blank");
        }
        if (normalizedTitle.length() > 200) {
            throw new IllegalArgumentException("title must be at most 200 characters");
        }
        Instant now = Instant.now(clock);
        if (!bettingDeadline.isAfter(now)) {
            throw new IllegalArgumentException("betting deadline must be in the future");
        }
        if (!resolutionDate.isAfter(bettingDeadline)) {
            throw new IllegalArgumentException("resolution date must be after betting deadline");
        }
        Stake validatedStake = new Stake(stake);
        Double validatedTolerance = validateTypeSpecifics(type, options, tolerance);

        Porra porra = new Porra();
        porra.emit(new PorraCreated(
                porraId,
                groupId,
                creatorId,
                normalizedTitle,
                description == null ? null : description.trim(),
                type,
                options == null ? List.of() : options.stream().map(String::trim).toList(),
                validatedTolerance,
                validatedStake.value(),
                bettingDeadline,
                resolutionDate,
                visibility,
                betModifiable == null || betModifiable,
                now));
        return porra;
    }

    public void deleteByCreator(UserId requesterId, Instant now, int totalBets) {
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        if (!requesterId.equals(creatorId)) {
            throw new IllegalArgumentException("only the creator can delete the porra");
        }
        if (status == PorraStatus.CANCELLED) {
            return;
        }
        boolean withinDeleteWindow = !now.isAfter(createdAt.plus(CREATOR_DELETE_WINDOW));
        if (!(totalBets == 0 || withinDeleteWindow)) {
            throw new IllegalStateException("porra cannot be deleted outside the allowed window");
        }
        emit(new PorraDeletedByCreator(id, groupId, now));
    }

    public void placeBet(UserId memberId, String rawPrediction, Instant now) {
        Objects.requireNonNull(memberId, "memberId cannot be null");
        Objects.requireNonNull(rawPrediction, "prediction cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        if (status != PorraStatus.OPEN || !bettingDeadline.isAfter(now)) {
            throw new IllegalStateException("betting deadline passed");
        }

        String normalizedPrediction = normalizePrediction(rawPrediction);
        Bet existingBet = bets.get(memberId);
        if (existingBet == null) {
            emit(new BetPlaced(id, groupId, memberId, normalizedPrediction, stake, now));
            return;
        }
        if (!betModifiable) {
            throw new IllegalArgumentException("already bet on this porra");
        }
        if (existingBet.prediction().equals(normalizedPrediction)) {
            return;
        }
        emit(new BetModified(id, memberId, existingBet.prediction(), normalizedPrediction, now));
    }

    public void closeBetting(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        if (status != PorraStatus.OPEN) {
            return;
        }
        if (bettingDeadline.isAfter(now)) {
            return;
        }
        if (bets.size() < 2) {
            emit(new PorraAutoCancelled(id, groupId, bettingDeadline));
            return;
        }
        List<BettingDeadlineReached.RevealedBet> revealedBets = bets.values().stream()
                .map(bet -> new BettingDeadlineReached.RevealedBet(
                        bet.memberId(),
                        bet.prediction(),
                        bet.placedAt()))
                .toList();
        emit(new BettingDeadlineReached(id, groupId, revealedBets, bettingDeadline));
    }

    public void enableResolution(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        if (status != PorraStatus.CLOSED) {
            return;
        }
        if (resolutionDate.isAfter(now)) {
            return;
        }
        emit(new ResolutionEnabled(id, groupId, resolutionDate));
    }

    private String normalizePrediction(String rawPrediction) {
        String trimmed = rawPrediction.trim();
        return switch (type) {
            case BINARY -> {
                if (!"true".equalsIgnoreCase(trimmed) && !"false".equalsIgnoreCase(trimmed)) {
                    throw new IllegalArgumentException("invalid binary prediction");
                }
                yield trimmed.toLowerCase();
            }
            case NUMERIC -> {
                try {
                    new BigDecimal(trimmed);
                    yield trimmed;
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("invalid numeric prediction");
                }
            }
            case MULTIPLE_CHOICE -> {
                if (options == null || options.stream().noneMatch(option -> option.equals(trimmed))) {
                    throw new IllegalArgumentException("invalid option");
                }
                yield trimmed;
            }
        };
    }

    private static Double validateTypeSpecifics(PorraType type, List<String> options, Double tolerance) {
        return switch (type) {
            case BINARY -> null;
            case NUMERIC -> (tolerance == null ? Tolerance.defaultTolerance() : new Tolerance(tolerance)).value();
            case MULTIPLE_CHOICE -> {
                if (options == null || options.size() < 2) {
                    throw new IllegalArgumentException("multiple choice porras need at least 2 options");
                }
                Set<String> uniqueOptions = new LinkedHashSet<>();
                for (String option : options) {
                    String normalized = new Option(option).value();
                    if (!uniqueOptions.add(normalized)) {
                        throw new IllegalArgumentException("options must be unique");
                    }
                }
                yield null;
            }
        };
    }

    @Override
    protected void apply(DomainEvent event) {
        if (event instanceof PorraCreated created) {
            id = created.porraId();
            groupId = created.groupId();
            creatorId = created.creatorId();
            title = created.title();
            description = created.description();
            type = created.type();
            options = created.options();
            tolerance = created.tolerance();
            stake = created.stake();
            bettingDeadline = created.bettingDeadline();
            resolutionDate = created.resolutionDate();
            visibility = created.visibility();
            betModifiable = created.betModifiable();
            status = PorraStatus.OPEN;
            createdAt = created.occurredAt();
        } else if (event instanceof PorraDeletedByCreator) {
            status = PorraStatus.CANCELLED;
        } else if (event instanceof BettingDeadlineReached) {
            status = PorraStatus.CLOSED;
        } else if (event instanceof PorraAutoCancelled) {
            status = PorraStatus.CANCELLED;
        } else if (event instanceof ResolutionEnabled) {
            status = PorraStatus.PENDING_RESOLUTION;
        } else if (event instanceof BetPlaced placed) {
            bets.put(placed.memberId(), new Bet(placed.memberId(), placed.prediction(), placed.occurredAt()));
        } else if (event instanceof BetModified modified) {
            bets.put(modified.memberId(), new Bet(modified.memberId(), modified.newPrediction(), modified.occurredAt()));
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        }
    }

    public PorraId id() {
        return id;
    }

    public PorraStatus status() {
        return status;
    }

    public UserId creatorId() {
        return creatorId;
    }

    public GroupId groupId() {
        return groupId;
    }

    public static Porra reconstitute(
            PorraId porraId,
            GroupId groupId,
            UserId creatorId,
            String title,
            String description,
            PorraType type,
            List<String> options,
            Double tolerance,
            int stake,
            Instant bettingDeadline,
            Instant resolutionDate,
            BetVisibility visibility,
            boolean betModifiable,
            PorraStatus status,
            Instant createdAt,
            List<Bet> existingBets,
            long version) {
        Porra porra = new Porra();
        porra.id = porraId;
        porra.groupId = groupId;
        porra.creatorId = creatorId;
        porra.title = title;
        porra.description = description;
        porra.type = type;
        porra.options = options;
        porra.tolerance = tolerance;
        porra.stake = stake;
        porra.bettingDeadline = bettingDeadline;
        porra.resolutionDate = resolutionDate;
        porra.visibility = visibility;
        porra.betModifiable = betModifiable;
        porra.status = status;
        porra.createdAt = createdAt;
        existingBets.forEach(bet -> porra.bets.put(bet.memberId(), bet));
        porra.rehydrateToVersion(version);
        return porra;
    }
}
