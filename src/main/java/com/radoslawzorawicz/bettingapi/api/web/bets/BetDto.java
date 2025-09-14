package com.radoslawzorawicz.bettingapi.api.web.bets;

import com.radoslawzorawicz.bettingapi.domain.bets.Bet;

import java.math.BigDecimal;

import static com.radoslawzorawicz.bettingapi.domain.bets.Bet.*;

record BetDto(
        Integer id,
        BigDecimal amount,
        String eventId,
        Integer driverId,
        Integer userId,
        BetStatus status,
        Integer odds
) {

    static BetDto toDto(Bet bet) {
        return new BetDto(
                bet.id(),
                bet.amount().toBigDecimal(),
                bet.eventId(),
                bet.driverId(),
                bet.userId(),
                bet.status(),
                bet.odds()
        );
    }
}
