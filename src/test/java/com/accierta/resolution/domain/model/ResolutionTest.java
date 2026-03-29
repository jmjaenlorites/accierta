package com.accierta.resolution.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResolutionTest {

    @Test
    void should_emit_resolution_proposed_with_48h_waiting_period() {
        Instant proposedAt = Instant.parse("2026-03-26T10:00:00Z");
        Resolution resolution = Resolution.propose(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                PorraType.MULTIPLE_CHOICE,
                List.of("A", "B"),
                "A",
                proposedAt);

        var event = (com.accierta.resolution.domain.event.ResolutionProposed)
                resolution.pullUncommittedEvents().getFirst();
        assertThat(event.waitingEndsAt()).isEqualTo(proposedAt.plusSeconds(48 * 3600));
        assertThat(resolution.status()).isEqualTo(ResolutionStatus.WAITING);
    }

    @Test
    void should_accept_none_of_the_above_for_multiple_choice() {
        Resolution resolution = Resolution.propose(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                PorraType.MULTIPLE_CHOICE,
                List.of("A", "B"),
                "NONE_OF_THE_ABOVE",
                Instant.parse("2026-03-26T10:00:00Z"));

        var event = (com.accierta.resolution.domain.event.ResolutionProposed)
                resolution.pullUncommittedEvents().getFirst();
        assertThat(event.proposedResult()).isEqualTo("NONE_OF_THE_ABOVE");
    }

    @Test
    void should_reject_none_of_the_above_for_binary() {
        assertThatThrownBy(() -> Resolution.propose(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                PorraType.BINARY,
                List.of(),
                "NONE_OF_THE_ABOVE",
                Instant.parse("2026-03-26T10:00:00Z")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid binary result");
    }

    @Test
    void should_emit_auto_accepted_when_waiting_period_passed() {
        Instant proposedAt = Instant.parse("2026-03-26T10:00:00Z");
        Resolution resolution = Resolution.propose(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                PorraType.BINARY,
                List.of(),
                "true",
                proposedAt);
        resolution.pullUncommittedEvents();

        resolution.autoAccept(proposedAt.plusSeconds(48 * 3600 + 1));

        assertThat(resolution.status()).isEqualTo(ResolutionStatus.ACCEPTED);
        assertThat(resolution.pullUncommittedEvents().getFirst())
                .isInstanceOf(com.accierta.resolution.domain.event.ResolutionAutoAccepted.class);
    }

    @Test
    void should_emit_resolution_disputed_and_open_voting() {
        Instant proposedAt = Instant.parse("2026-03-26T10:00:00Z");
        Resolution resolution = Resolution.propose(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                PorraType.MULTIPLE_CHOICE,
                List.of("A", "B"),
                "A",
                proposedAt);
        resolution.pullUncommittedEvents();

        resolution.dispute(UserId.newId(), "No estoy de acuerdo", proposedAt.plusSeconds(60));

        assertThat(resolution.status()).isEqualTo(ResolutionStatus.VOTING);
        assertThat(resolution.pullUncommittedEvents().getFirst())
                .isInstanceOf(com.accierta.resolution.domain.event.ResolutionDisputed.class);
    }

    @Test
    void should_tally_majority_vote() {
        Instant proposedAt = Instant.parse("2026-03-26T10:00:00Z");
        UserId proposer = UserId.newId();
        UserId disputer = UserId.newId();
        Resolution resolution = Resolution.propose(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                proposer,
                PorraType.MULTIPLE_CHOICE,
                List.of("A", "B"),
                "A",
                proposedAt);
        resolution.pullUncommittedEvents();
        resolution.dispute(disputer, null, proposedAt.plusSeconds(10));
        resolution.pullUncommittedEvents();
        resolution.castVote(UserId.newId(), PorraType.MULTIPLE_CHOICE, List.of("A", "B"), "B", proposedAt.plusSeconds(20));
        resolution.castVote(UserId.newId(), PorraType.MULTIPLE_CHOICE, List.of("A", "B"), "B", proposedAt.plusSeconds(30));
        resolution.castVote(UserId.newId(), PorraType.MULTIPLE_CHOICE, List.of("A", "B"), "A", proposedAt.plusSeconds(40));
        resolution.pullUncommittedEvents();

        resolution.tallyVotes(proposedAt.plusSeconds(48 * 3600 + 11));

        assertThat(resolution.status()).isEqualTo(ResolutionStatus.ACCEPTED);
        var event = (com.accierta.resolution.domain.event.ResolutionAccepted) resolution.pullUncommittedEvents().getFirst();
        assertThat(event.acceptedResult()).isEqualTo("B");
        assertThat(event.method()).isEqualTo(ResolutionMethod.VOTE);
    }

    @Test
    void should_tally_no_votes_as_consensus() {
        Instant proposedAt = Instant.parse("2026-03-26T10:00:00Z");
        Resolution resolution = Resolution.propose(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                PorraType.BINARY,
                List.of(),
                "true",
                proposedAt);
        resolution.pullUncommittedEvents();
        resolution.dispute(UserId.newId(), null, proposedAt.plusSeconds(10));
        resolution.pullUncommittedEvents();

        resolution.tallyVotes(proposedAt.plusSeconds(48 * 3600 + 11));

        var event = (com.accierta.resolution.domain.event.ResolutionAccepted) resolution.pullUncommittedEvents().getFirst();
        assertThat(event.acceptedResult()).isEqualTo("true");
        assertThat(event.method()).isEqualTo(ResolutionMethod.CONSENSUS);
    }

    @Test
    void should_tally_tie_with_tiebreak_method() {
        Instant proposedAt = Instant.parse("2026-03-26T10:00:00Z");
        Resolution resolution = Resolution.propose(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                PorraType.MULTIPLE_CHOICE,
                List.of("A", "B"),
                "A",
                proposedAt);
        resolution.pullUncommittedEvents();
        resolution.dispute(UserId.newId(), null, proposedAt.plusSeconds(10));
        resolution.pullUncommittedEvents();
        resolution.castVote(UserId.newId(), PorraType.MULTIPLE_CHOICE, List.of("A", "B"), "A", proposedAt.plusSeconds(20));
        resolution.castVote(UserId.newId(), PorraType.MULTIPLE_CHOICE, List.of("A", "B"), "B", proposedAt.plusSeconds(30));
        resolution.pullUncommittedEvents();

        resolution.tallyVotes(proposedAt.plusSeconds(48 * 3600 + 11));

        var event = (com.accierta.resolution.domain.event.ResolutionAccepted) resolution.pullUncommittedEvents().getFirst();
        assertThat(event.method()).isEqualTo(ResolutionMethod.TIEBREAK);
        assertThat(event.acceptedResult()).isIn("A", "B");
    }

    @Test
    void should_propose_cancellation_and_auto_accept_it() {
        Instant proposedAt = Instant.parse("2026-03-26T10:00:00Z");
        Resolution resolution = Resolution.proposeCancellation(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Event suspended",
                proposedAt);
        resolution.pullUncommittedEvents();

        resolution.autoAccept(proposedAt.plusSeconds(48 * 3600 + 1));

        assertThat(resolution.status()).isEqualTo(ResolutionStatus.ACCEPTED);
        assertThat(resolution.proposalType()).isEqualTo(ResolutionProposalType.CANCELLATION);
        assertThat(resolution.pullUncommittedEvents().getFirst())
                .isInstanceOf(com.accierta.resolution.domain.event.PorraCancelled.class);
    }

    @Test
    void should_reject_cancellation_when_majority_votes_keep() {
        Instant proposedAt = Instant.parse("2026-03-26T10:00:00Z");
        Resolution resolution = Resolution.proposeCancellation(
                ResolutionId.newId(),
                PorraId.newId(),
                GroupId.newId(),
                UserId.newId(),
                "Event suspended",
                proposedAt);
        resolution.pullUncommittedEvents();
        resolution.dispute(UserId.newId(), null, proposedAt.plusSeconds(10));
        resolution.pullUncommittedEvents();
        resolution.castVote(UserId.newId(), PorraType.MULTIPLE_CHOICE, List.of("A", "B"), "KEEP", proposedAt.plusSeconds(20));
        resolution.castVote(UserId.newId(), PorraType.MULTIPLE_CHOICE, List.of("A", "B"), "KEEP", proposedAt.plusSeconds(30));
        resolution.pullUncommittedEvents();

        resolution.tallyVotes(proposedAt.plusSeconds(48 * 3600 + 11));

        assertThat(resolution.status()).isEqualTo(ResolutionStatus.REJECTED);
        assertThat(resolution.pullUncommittedEvents().getFirst())
                .isInstanceOf(com.accierta.resolution.domain.event.CancellationRejected.class);
    }
}
