package com.radoslawzorawicz.bettingapi.api.web.bets;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceBetRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldAcceptNonNegativeWithAtMostTwoDecimals() {
        // given
        var req = new PlaceBetRequest("e1", 1, new BigDecimal("10.50"));

        // when
        final Set<ConstraintViolation<PlaceBetRequest>> violations = validator.validate(req);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectZero() {
        var req = new PlaceBetRequest("e1", 1, new BigDecimal("0.00"));
        Set<ConstraintViolation<PlaceBetRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectNegative() {
        final var req = new PlaceBetRequest("e1", 1, new BigDecimal("-1.00"));
        final Set<ConstraintViolation<PlaceBetRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectMoreThanTwoDecimals() {
        final var req = new PlaceBetRequest("e1", 1, new BigDecimal("1.001"));
        Set<ConstraintViolation<PlaceBetRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectBlankEventId() {
        final var req = new PlaceBetRequest("  ", 1, new BigDecimal("10.00"));
        final Set<ConstraintViolation<PlaceBetRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectNonPositiveDriverId() {
        final var reqZero = new PlaceBetRequest("e1", 0, new BigDecimal("10.00"));
        final var reqNegative = new PlaceBetRequest("e1", -5, new BigDecimal("10.00"));
        assertThat(validator.validate(reqZero)).isNotEmpty();
        assertThat(validator.validate(reqNegative)).isNotEmpty();
    }
}
