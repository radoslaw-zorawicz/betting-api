package com.radoslawzorawicz.bettingapi.domain.bets;

import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import lombok.AccessLevel;
import lombok.With;

@With(AccessLevel.PRIVATE)
public record Bet(
        Integer id,
        String eventId,
        Integer driverId,
        Integer userId,
        Money amount,
        BetStatus status,
        Integer odds
) {
    public Bet resolveByDriverId(Integer winningDriverId) {
        if (status != BetStatus.PENDING) {
            return this;
        }
        return this.driverId.equals(winningDriverId)
                ? this.withStatus(BetStatus.WON)
                : this.withStatus(BetStatus.LOST);
    }

    public Money calculatePayout() {
        return amount.multiply(odds);
    }

    public boolean isWon() {
        return status == BetStatus.WON;
    }

    public enum BetStatus {
        PENDING,
        WON,
        LOST
    }

}
