package com.radoslawzorawicz.bettingapi.domain.events.service;

import com.radoslawzorawicz.bettingapi.domain.events.model.Odds;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomOddsPolicy implements OddsPolicy {
    private static final int[] VALUES = {2, 3, 4};

    @Override
    public Odds nextOdds() {
        return Odds.of(VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)]);
    }
}
