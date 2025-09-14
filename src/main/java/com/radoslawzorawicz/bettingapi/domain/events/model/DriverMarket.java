package com.radoslawzorawicz.bettingapi.domain.events.model;

import com.radoslawzorawicz.bettingapi.domain.events.service.OddsPolicy;

public record DriverMarket(
        Driver driver,
        int odds
) {
    public static DriverMarket from(Driver driver, OddsPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("policy must not be null");
        return new DriverMarket(driver, policy.nextOdds().value());
    }
}
