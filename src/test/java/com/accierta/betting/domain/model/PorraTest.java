package com.accierta.betting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class PorraTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Test
    void should_emit_created_event_for_binary_porra() {
        Porra porra = Porra.create(
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Who wins?",
                null,
                PorraType.BINARY,
                null,
                null,
                50,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true,
                FIXED_CLOCK);

        assertThat(porra.pullUncommittedEvents()).hasSize(1);
        assertThat(porra.status()).isEqualTo(PorraStatus.OPEN);
    }

    @Test
    void should_default_numeric_tolerance_to_015() {
        Porra porra = Porra.create(
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Goals?",
                null,
                PorraType.NUMERIC,
                null,
                null,
                50,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true,
                FIXED_CLOCK);

        var event = (com.accierta.betting.domain.event.PorraCreated) porra.pullUncommittedEvents().getFirst();
        assertThat(event.tolerance()).isEqualTo(0.15d);
    }

    @Test
    void should_reject_multiple_choice_with_single_option() {
        assertThatThrownBy(() -> Porra.create(
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Options",
                null,
                PorraType.MULTIPLE_CHOICE,
                List.of("A"),
                null,
                50,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.VISIBLE,
                true,
                FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("multiple choice porras need at least 2 options");
    }

    @Test
    void should_emit_bet_modified_when_member_bets_again_and_modifiable_is_true() {
        UserId memberId = UserId.newId();
        Porra porra = Porra.create(
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Who wins?",
                null,
                PorraType.BINARY,
                null,
                null,
                50,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true,
                FIXED_CLOCK);
        porra.pullUncommittedEvents();

        porra.placeBet(memberId, "true", FIXED_CLOCK.instant().plusSeconds(10));
        porra.pullUncommittedEvents();
        porra.placeBet(memberId, "false", FIXED_CLOCK.instant().plusSeconds(20));

        assertThat(porra.pullUncommittedEvents().getFirst())
                .isInstanceOf(com.accierta.betting.domain.event.BetModified.class);
    }

    @Test
    void should_close_porra_when_deadline_passed_and_there_are_two_bets() {
        Porra porra = Porra.create(
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Who wins?",
                null,
                PorraType.BINARY,
                null,
                null,
                50,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true,
                FIXED_CLOCK);
        porra.pullUncommittedEvents();
        porra.placeBet(UserId.newId(), "true", FIXED_CLOCK.instant().plusSeconds(10));
        porra.placeBet(UserId.newId(), "false", FIXED_CLOCK.instant().plusSeconds(20));
        porra.pullUncommittedEvents();

        porra.closeBetting(FIXED_CLOCK.instant().plusSeconds(3601));

        assertThat(porra.status()).isEqualTo(PorraStatus.CLOSED);
        assertThat(porra.pullUncommittedEvents().getFirst())
                .isInstanceOf(com.accierta.betting.domain.event.BettingDeadlineReached.class);
    }

    @Test
    void should_auto_cancel_porra_when_deadline_passed_with_fewer_than_two_bets() {
        Porra porra = Porra.create(
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Who wins?",
                null,
                PorraType.BINARY,
                null,
                null,
                50,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true,
                FIXED_CLOCK);
        porra.pullUncommittedEvents();
        porra.placeBet(UserId.newId(), "true", FIXED_CLOCK.instant().plusSeconds(10));
        porra.pullUncommittedEvents();

        porra.closeBetting(FIXED_CLOCK.instant().plusSeconds(3601));

        assertThat(porra.status()).isEqualTo(PorraStatus.CANCELLED);
        assertThat(porra.pullUncommittedEvents().getFirst())
                .isInstanceOf(com.accierta.betting.domain.event.PorraAutoCancelled.class);
    }

    @Test
    void should_enable_resolution_when_resolution_date_passed_for_closed_porra() {
        Porra porra = Porra.create(
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Who wins?",
                null,
                PorraType.BINARY,
                null,
                null,
                50,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true,
                FIXED_CLOCK);
        porra.pullUncommittedEvents();
        porra.placeBet(UserId.newId(), "true", FIXED_CLOCK.instant().plusSeconds(10));
        porra.placeBet(UserId.newId(), "false", FIXED_CLOCK.instant().plusSeconds(20));
        porra.pullUncommittedEvents();
        porra.closeBetting(FIXED_CLOCK.instant().plusSeconds(3601));
        porra.pullUncommittedEvents();

        porra.enableResolution(FIXED_CLOCK.instant().plusSeconds(7201));

        assertThat(porra.status()).isEqualTo(PorraStatus.PENDING_RESOLUTION);
        assertThat(porra.pullUncommittedEvents().getFirst())
                .isInstanceOf(com.accierta.betting.domain.event.ResolutionEnabled.class);
    }
}
