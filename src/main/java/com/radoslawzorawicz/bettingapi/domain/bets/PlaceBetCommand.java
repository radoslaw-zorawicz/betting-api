package com.radoslawzorawicz.bettingapi.domain.bets;

import java.math.BigDecimal;

public record PlaceBetCommand(
        String eventId,
        Integer driverId,
        BigDecimal betAmount
) {

}
