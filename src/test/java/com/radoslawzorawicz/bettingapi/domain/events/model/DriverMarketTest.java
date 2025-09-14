package com.radoslawzorawicz.bettingapi.domain.events.model;

import com.radoslawzorawicz.bettingapi.domain.events.service.OddsPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverMarketTest {

    @Test
    void shouldCreateFromPolicy() {
        // given
        final Driver driver = new Driver(44, "Lewis Hamilton", "Mercedes");
        final OddsPolicy fixedPolicy = () -> Odds.of(3);

        // when
        final DriverMarket market = DriverMarket.from(driver, fixedPolicy);

        // then
        assertThat(market).isEqualTo(new DriverMarket(driver, 3));
    }

    @Test
    void shouldThrowOnNullPolicy() {
        // given
        final Driver driver = new Driver(1, "Driver", "Team");

        // expect
        assertThatThrownBy(() -> DriverMarket.from(driver, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policy must not be null");
    }
}
