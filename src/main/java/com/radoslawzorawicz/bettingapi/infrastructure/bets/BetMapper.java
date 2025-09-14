package com.radoslawzorawicz.bettingapi.infrastructure.bets;

import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;

public final class BetMapper {

    public Bet toDomain(BetJpaEntity betJpaEntity) {
        return new Bet(
                betJpaEntity.getId(),
                betJpaEntity.getEventId(),
                betJpaEntity.getDriverId(),
                betJpaEntity.getUserId(),
                Money.of(betJpaEntity.getAmount()),
                betJpaEntity.getStatus(),
                betJpaEntity.getOdds()
        );
    }

    public BetJpaEntity toEntity(Bet bet) {
        return new BetJpaEntity(
                bet.id(), bet.eventId(), bet.driverId(), bet.userId(), bet.amount().toBigDecimal(), bet.status(), bet.odds()
        );
    }
}
