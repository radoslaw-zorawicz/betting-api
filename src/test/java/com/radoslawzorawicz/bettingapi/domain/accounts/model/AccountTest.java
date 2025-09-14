package com.radoslawzorawicz.bettingapi.domain.accounts.model;

import com.radoslawzorawicz.bettingapi.domain.accounts.Account;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import io.vavr.control.Option;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    @Test
    void shouldCreditIncreaseBalance() {
        // given
        final Account account = new Account(1, 99, Money.of(new BigDecimal("10.00")));

        // when
        final Account credited = account.credit(Money.of(new BigDecimal("2.50")));

        // then
        assertThat(credited.accountBalance().toBigDecimal()).isEqualByComparingTo("12.50");
    }

    @Test
    void shouldDebitWhenSufficientFunds() {
        // given
        final Account account = new Account(1, 99, Money.of(new BigDecimal("10.00")));

        // when
        final Option<Account> updated = account.debit(Money.of(new BigDecimal("7.50")));

        // then
        final var expectedBalance = Money.of(new BigDecimal("2.50"));
        VavrAssertions.assertThat(updated)
                .hasValueSatisfying(updatedAccount -> assertThat(updatedAccount)
                        .extracting(Account::accountBalance)
                        .isEqualTo(expectedBalance)
                );
    }

    @Test
    void shouldNotDebitWhenInsufficientFunds() {
        // given
        final Account account = new Account(1, 99, Money.of(new BigDecimal("5.00")));

        // when
        final Option<Account> updated = account.debit(Money.of(new BigDecimal("6.00")));

        // then
        VavrAssertions.assertThat(updated).isEmpty();
    }

    @Test
    void shouldDebitExactBalanceToZero() {
        // given
        final Account account = new Account(1, 42, Money.of(new BigDecimal("10.00")));

        // when
        final Option<Account> updated = account.debit(Money.of(new BigDecimal("10.00")));

        // then
        VavrAssertions.assertThat(updated)
                .hasValueSatisfying(a -> assertThat(a.accountBalance()).isEqualTo(Money.zero()));
    }

    @Test
    void shouldCreditZeroKeepBalanceUnchanged() {
        // given
        final Account account = new Account(1, 7, Money.of(new BigDecimal("10.00")));

        // when
        final Account credited = account.credit(Money.of(new BigDecimal("0.00")));

        // then
        assertThat(credited.accountBalance()).isEqualTo(Money.of(new BigDecimal("10.00")));
    }
}
