package com.radoslawzorawicz.bettingapi.domain.bets.service;

import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.SettlementPolicy;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementPolicyTest {

    private final SettlementPolicy policy = new SettlementPolicy();

    @Test
    void shouldResolveStatuses() {
        // given
        final var bets = List.of(
                new Bet(1, "e", 10, 1, Money.of(new BigDecimal("5.00")), Bet.BetStatus.PENDING, 2),
                new Bet(2, "e", 11, 2, Money.of(new BigDecimal("5.00")), Bet.BetStatus.PENDING, 3)
        );

        // when
        final var resolved = policy.resolve(bets, 10);

        // then
        assertThat(resolved)
                .hasSize(2)
                .extracting(Bet::status)
                .containsExactly(Bet.BetStatus.WON, Bet.BetStatus.LOST);
    }

    @Test
    void shouldAggregateCreditsPerUser() {
        // given
        final var bets = List.of(
                new Bet(1, "e", 10, 1, Money.of(new BigDecimal("5.00")), Bet.BetStatus.WON, 2), // 10
                new Bet(2, "e", 10, 1, Money.of(new BigDecimal("3.00")), Bet.BetStatus.WON, 3), // 9
                new Bet(3, "e", 10, 2, Money.of(new BigDecimal("4.00")), Bet.BetStatus.WON, 4)  // 16
        );

        // when
        final Map<Integer, Money> credits = policy.creditsByUser(bets);

        // then
        assertThat(credits).hasSize(2);
        assertThat(credits.get(1).toBigDecimal()).isEqualByComparingTo("19.00");
        assertThat(credits.get(2).toBigDecimal()).isEqualByComparingTo("16.00");
    }
}

