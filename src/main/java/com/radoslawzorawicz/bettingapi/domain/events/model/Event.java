package com.radoslawzorawicz.bettingapi.domain.events.model;

import java.time.OffsetDateTime;

public record Event(
        String eventId,
        String name,
        String sessionType,
        int year,
        String country,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}
