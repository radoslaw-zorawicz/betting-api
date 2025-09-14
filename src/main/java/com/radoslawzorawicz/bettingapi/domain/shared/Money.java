package com.radoslawzorawicz.bettingapi.domain.shared;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Value
public class Money {
    private static final int SCALE = 2;
    BigDecimal value;

    private Money(BigDecimal value) {
        this.value = value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal value) {
        Objects.requireNonNull(value, "value");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money value must be non-negative");
        }
        return new Money(value);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        Objects.requireNonNull(other, "other");
        final var result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resulting money must be non-negative");
        }
        return new Money(result);
    }

    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier must be non-negative");
        }
        return new Money(this.value.multiply(BigDecimal.valueOf(multiplier)));
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    public int compare(Money other) {
        Objects.requireNonNull(other, "other");
        return this.value.compareTo(other.value);
    }
}
