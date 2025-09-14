package com.radoslawzorawicz.bettingapi.api.web.events;

import jakarta.validation.constraints.NotNull;

record SettleEventRequest(
        @NotNull Integer winningDriverId
) {}
