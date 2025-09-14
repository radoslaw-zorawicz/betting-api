package com.radoslawzorawicz.bettingapi.domain.bets.model;

import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BetTest {

    @Test
    void shouldResolvePendingBetToWon() {
        // given
        final Bet pending = new Bet(null, "e", 44, 7, Money.of(new BigDecimal("10.00")), Bet.BetStatus.PENDING, 3);

        // when
        final Bet won = pending.resolveByDriverId(44);

        // then
        assertThat(won.status()).isEqualTo(Bet.BetStatus.WON);
    }

    @Test
    void shouldResolvePendingBetToLost() {
        // given
        final Bet pending = new Bet(null, "e", 44, 7, Money.of(new BigDecimal("10.00")), Bet.BetStatus.PENDING, 3);

        // when
        final Bet lost = pending.resolveByDriverId(16);

        // then
        assertThat(lost.status()).isEqualTo(Bet.BetStatus.LOST);
    }

    @Test
    void shouldNotChangeAlreadyResolvedBet() {
        // given
        final Bet won = new Bet(null, "e", 44, 7, Money.of(new BigDecimal("10.00")), Bet.BetStatus.WON, 3);

        // when
        final Bet resolved = won.resolveByDriverId(16);

        // then
        assertThat(resolved).isSameAs(won);
    }

    @Test
    void shouldCalculatePayoutWithCorrectScale() {
        // given
        final Bet bet = new Bet(null, "e", 44, 7, Money.of(new BigDecimal("10.00")), Bet.BetStatus.PENDING, 3);

        // when
        final Money payout = bet.calculatePayout();

        // then
        assertThat(payout.toBigDecimal()).isEqualByComparingTo("30.00");
    }
}
