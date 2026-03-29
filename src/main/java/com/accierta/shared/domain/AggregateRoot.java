package com.accierta.shared.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {

    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    private long version;

    protected void emit(DomainEvent event) {
        apply(event);
        uncommittedEvents.add(event);
        version++;
    }

    protected abstract void apply(DomainEvent event);

    protected void replay(DomainEvent event) {
        apply(event);
        version++;
    }

    protected void rehydrateToVersion(long version) {
        this.version = version;
    }

    public List<DomainEvent> pullUncommittedEvents() {
        List<DomainEvent> events = List.copyOf(uncommittedEvents);
        uncommittedEvents.clear();
        return events;
    }

    public long version() {
        return version;
    }
}
