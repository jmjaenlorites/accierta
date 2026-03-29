package com.accierta.groups.domain.model;

import com.accierta.groups.domain.event.GroupCreated;
import com.accierta.groups.domain.event.MemberJoined;
import com.accierta.shared.domain.AggregateRoot;
import com.accierta.shared.domain.DomainEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Group extends AggregateRoot {

    private GroupId id;
    private GroupName name;
    private Currency currency;
    private final Set<MemberId> members = new HashSet<>();

    private Group() {
    }

    public static Group create(
            GroupId groupId,
            GroupName groupName,
            Currency currency,
            MemberId creatorId,
            Clock clock) {
        Objects.requireNonNull(groupId, "groupId cannot be null");
        Objects.requireNonNull(groupName, "groupName cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        Objects.requireNonNull(creatorId, "creatorId cannot be null");
        Objects.requireNonNull(clock, "clock cannot be null");

        Group group = new Group();
        Instant now = Instant.now(clock);
        group.emit(new GroupCreated(groupId, groupName, currency, creatorId, now));
        group.emit(new MemberJoined(groupId, creatorId, now));
        return group;
    }

    @Override
    protected void apply(DomainEvent event) {
        if (event instanceof GroupCreated created) {
            this.id = created.groupId();
            this.name = created.groupName();
            this.currency = created.currency();
        } else if (event instanceof MemberJoined joined) {
            this.members.add(joined.memberId());
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        }
    }

    public GroupId id() {
        return id;
    }

    public GroupName name() {
        return name;
    }

    public Currency currency() {
        return currency;
    }

    public Set<MemberId> members() {
        return Set.copyOf(members);
    }
}
