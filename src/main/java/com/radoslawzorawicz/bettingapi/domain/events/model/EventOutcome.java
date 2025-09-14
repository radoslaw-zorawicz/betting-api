package com.radoslawzorawicz.bettingapi.domain.events.model;

import java.time.OffsetDateTime;

public record EventOutcome(
        String eventId,
        Integer winningDriverId,
        OffsetDateTime finishedAt
) {}

