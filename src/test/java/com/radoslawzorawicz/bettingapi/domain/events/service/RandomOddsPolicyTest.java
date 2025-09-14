package com.radoslawzorawicz.bettingapi.domain.events.service;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RandomOddsPolicyTest {

    @Test
    void shouldGeneratePositiveOddsWithinAllowedSet() {
        // given
        final RandomOddsPolicy policy = new RandomOddsPolicy();
        final Set<Integer> allowed = Set.of(2, 3, 4);

        // when
        final var generated = IntStream.range(0, 100)
                .map(_ -> policy.nextOdds().value())
                .boxed()
                .toList();

        // then
        assertThat(generated)
                .isNotEmpty()
                .allMatch(allowed::contains);
    }
}
