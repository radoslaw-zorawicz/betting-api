package com.radoslawzorawicz.bettingapi.domain.events.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OddsTest {

    @Test
    void shouldCreatePositiveOdds() {
        // when
        final Odds odds = Odds.of(3);

        // then
        assertThat(odds).isEqualTo(Odds.of(3));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectNonPositiveOdds(int invalid) {
        assertThatThrownBy(() -> Odds.of(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }
}
