package com.radoslawzorawicz.bettingapi.domain.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldCreateNonNegativeMoney() {
        // when
        final Money ten = Money.of(new BigDecimal("10"));

        // then
        assertThat(ten.toBigDecimal()).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldNormalizeScaleAndEquality() {
        // given
        final Money a = Money.of(new BigDecimal("10"));
        final Money b = Money.of(new BigDecimal("10.0"));

        // then
        assertThat(a).isEqualTo(b);
        assertThat(a.toBigDecimal().scale()).isEqualTo(2);
    }

    @Test
    void shouldThrowOnNegativeCreation() {
        // expect
        assertThatThrownBy(() -> Money.of(new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void shouldAdd() {
        // given
        final Money five = Money.of(new BigDecimal("5"));
        final Money three = Money.of(new BigDecimal("3"));

        // when
        final Money sum = five.add(three);

        // then
        assertThat(sum.toBigDecimal()).isEqualByComparingTo("8.00");
    }

    @Test
    void shouldMultiply() {
        // given
        final Money three = Money.of(new BigDecimal("3"));

        // when
        final Money multi = three.multiply(4);

        // then
        assertThat(multi.toBigDecimal()).isEqualByComparingTo("12.00");
    }

    @Test
    void shouldRejectNegativeMultiplier() {
        // given
        final Money one = Money.of(BigDecimal.ONE);

        // expect
        assertThatThrownBy(() -> one.multiply(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void shouldSubtract() {
        // given
        final Money ten = Money.of(new BigDecimal("10.00"));
        final Money three = Money.of(new BigDecimal("3.00"));

        // when
        final Money result = ten.subtract(three);

        // then
        assertThat(result.toBigDecimal()).isEqualByComparingTo("7.00");
    }

    @Test
    void shouldThrowOnNegativeSubtractResult() {
        // given
        final Money five = Money.of(new BigDecimal("5.00"));

        // expect
        assertThatThrownBy(() -> five.subtract(Money.of(new BigDecimal("6.00"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void shouldCompareGreaterThan() {
        // given
        final Money five = Money.of(new BigDecimal("5.00"));
        final Money ten = Money.of(new BigDecimal("10.00"));

        // then
        assertThat(ten.compare(five)).isGreaterThan(0);
    }

    @Test
    void shouldCompareLessThan() {
        // given
        final Money five = Money.of(new BigDecimal("5.00"));
        final Money ten = Money.of(new BigDecimal("10.00"));

        // then
        assertThat(five.compare(ten)).isLessThan(0);
    }

    @Test
    void shouldCompareEqual() {
        // given
        final Money tenA = Money.of(new BigDecimal("10.00"));
        final Money tenB = Money.of(new BigDecimal("10.00"));

        // then
        assertThat(tenA.compare(tenB)).isZero();
    }

    @Test
    void shouldThrowOnNullCompareArgument() {
        // expect
        assertThatThrownBy(() -> Money.of(new BigDecimal("1.00")).compare(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("other");
    }

    @Test
    void shouldReturnZeroOnMultiplyByZero() {
        // given
        final Money threeThirtyThree = Money.of(new BigDecimal("3.33"));

        // when
        final Money result = threeThirtyThree.multiply(0);

        // then
        assertThat(result)
                .isEqualTo(Money.zero());
        assertThat(result.toBigDecimal().scale()).isEqualTo(2);
    }

    @Test
    void shouldThrowOnNullSubtractArgument() {
        // expect
        assertThatThrownBy(() -> Money.of(new BigDecimal("1.00")).subtract(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("other");
    }
}
