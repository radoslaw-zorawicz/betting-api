package com.radoslawzorawicz.bettingapi.infrastructure.events;

import com.radoslawzorawicz.bettingapi.domain.events.model.DomainEventPublisher;
import com.radoslawzorawicz.bettingapi.domain.events.model.EventFinished;
import com.radoslawzorawicz.bettingapi.domain.events.model.EventFinishedHandler;

public class InMemoryDomainEventPublisher implements DomainEventPublisher {

    private final EventFinishedHandler handler;

    public InMemoryDomainEventPublisher(EventFinishedHandler handler) {
        this.handler = handler;
    }

    @Override
    public void publish(EventFinished event) {
        handler.handle(event);
    }
}
