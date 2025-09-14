package com.radoslawzorawicz.bettingapi.domain.events.model;

public interface DomainEventPublisher {
    void publish(EventFinished event);
}
