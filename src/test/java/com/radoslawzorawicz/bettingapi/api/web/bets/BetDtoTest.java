package com.radoslawzorawicz.bettingapi.api.web.bets;

import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BetDtoTest {

    @Test
    void shouldMapDomainToDtoWithAllFields() {
        // given
        final var bet = new Bet(10, "EVT", 44, 7, Money.of(new BigDecimal("12.34")), Bet.BetStatus.PENDING, 3);

        // when
        final BetDto dto = BetDto.toDto(bet);

        // then
        final var expected = new BetDto(10, new BigDecimal("12.34"), "EVT", 44, 7, Bet.BetStatus.PENDING, 3);
        assertThat(dto).isEqualTo(expected);
    }
}

