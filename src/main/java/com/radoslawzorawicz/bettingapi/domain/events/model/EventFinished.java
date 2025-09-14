package com.radoslawzorawicz.bettingapi.domain.events.model;

import java.time.OffsetDateTime;

public record EventFinished(
        String eventId,
        Integer winningDriverId,
        OffsetDateTime finishedAt
) {}
