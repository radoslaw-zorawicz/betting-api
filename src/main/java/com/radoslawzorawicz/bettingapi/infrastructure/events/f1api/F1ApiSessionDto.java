package com.radoslawzorawicz.bettingapi.infrastructure.events.f1api;

import com.radoslawzorawicz.bettingapi.domain.events.model.Event;

import java.time.OffsetDateTime;

record F1ApiSessionDto(
        String sessionKey,
        String sessionName,
        String sessionType,
        Integer year,
        String country,
        OffsetDateTime dateStart,
        OffsetDateTime dateEnd
) {
    Event toEvent() {
        return new Event(
                sessionKey, sessionName, sessionType, year, country, dateStart, dateEnd
        );
    }
}
