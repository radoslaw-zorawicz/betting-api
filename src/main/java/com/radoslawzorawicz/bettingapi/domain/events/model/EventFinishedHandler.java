package com.radoslawzorawicz.bettingapi.domain.events.model;

public interface EventFinishedHandler {
    void handle(EventFinished event);
}
