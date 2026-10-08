package pe.greenminds.ecomind.quests.domain.model.aggregates;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractDomainAggregateRoot<T> {
    private final List<Object> domainEvents = new ArrayList<>();

    protected void registerEvent(Object event) {
        domainEvents.add(event);
    }

    protected void registerDomainEvent(Object event) {
        domainEvents.add(event);
    }

    public List<Object> domainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
