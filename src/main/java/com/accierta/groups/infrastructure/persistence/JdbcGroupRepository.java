package com.accierta.groups.infrastructure.persistence;

import com.accierta.groups.domain.event.GroupCreated;
import com.accierta.groups.domain.event.MemberJoined;
import com.accierta.groups.domain.model.Group;
import com.accierta.groups.domain.port.out.GroupRepository;
import com.accierta.shared.domain.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcGroupRepository implements GroupRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcGroupRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void save(Group group) {
        List<DomainEvent> newEvents = group.pullUncommittedEvents();
        long initialVersion = group.version() - newEvents.size();

        for (int index = 0; index < newEvents.size(); index++) {
            DomainEvent event = newEvents.get(index);
            jdbcTemplate.update(
                    """
                    insert into groups_event_store (aggregate_id, version, event_type, payload, occurred_at)
                    values (?, ?, ?, cast(? as jsonb), ?)
                    """,
                    group.id().value(),
                    initialVersion + index + 1,
                    event.getClass().getSimpleName(),
                    serialize(event),
                    Timestamp.from(event.occurredAt()));

            if (event instanceof GroupCreated created) {
                jdbcTemplate.update(
                        """
                        insert into groups_projection (group_id, name, currency, created_at)
                        values (?, ?, ?, ?)
                        """,
                        created.groupId().value(),
                        created.groupName().value(),
                        created.currency().value(),
                        Timestamp.from(created.occurredAt()));
            } else if (event instanceof MemberJoined joined) {
                jdbcTemplate.update(
                        """
                        insert into group_members_projection (group_id, user_id, joined_at)
                        values (?, ?, ?)
                        """,
                        joined.groupId().value(),
                        joined.memberId().value().value(),
                        Timestamp.from(joined.occurredAt()));
            }
        }
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize event", exception);
        }
    }
}
