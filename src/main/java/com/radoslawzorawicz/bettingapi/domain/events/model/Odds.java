package com.radoslawzorawicz.bettingapi.domain.events.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Odds {
    int value;

    public static Odds of(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("odds must be positive");
        }
        return new Odds(value);
    }

    public int value() {
        return value;
    }
}

