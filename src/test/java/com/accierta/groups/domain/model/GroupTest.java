package com.accierta.groups.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accierta.groups.domain.event.GroupCreated;
import com.accierta.groups.domain.event.MemberJoined;
import com.accierta.identity.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class GroupTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Test
    void should_emit_group_created_and_member_joined_with_default_currency() {
        Group group = Group.create(
                GroupId.newId(),
                new GroupName("Fantasy Liga"),
                Currency.defaultCurrency(),
                new MemberId(UserId.newId()),
                FIXED_CLOCK);

        var events = group.pullUncommittedEvents();

        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isInstanceOf(GroupCreated.class);
        assertThat(events.get(1)).isInstanceOf(MemberJoined.class);
        assertThat(group.currency().value()).isEqualTo("\u20ac");
    }

    @Test
    void should_support_custom_currency() {
        Group group = Group.create(
                GroupId.newId(),
                new GroupName("Fantasy Liga"),
                new Currency("🍺"),
                new MemberId(UserId.newId()),
                FIXED_CLOCK);

        assertThat(group.currency().value()).isEqualTo("🍺");
    }

    @Test
    void should_reject_blank_group_name() {
        assertThatThrownBy(() -> new GroupName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("group name cannot be blank");
    }
}
