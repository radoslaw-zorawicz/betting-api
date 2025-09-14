package com.radoslawzorawicz.bettingapi.domain.events.model;

import io.vavr.control.Option;

public interface EventOutcomeRepository {
    Option<EventOutcome> findById(String eventId);

    Option<EventOutcome> save(EventOutcome outcome);
}
