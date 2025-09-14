package com.radoslawzorawicz.bettingapi.domain.accounts;

import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import io.vavr.control.Option;

public record Account(Integer id, Integer userId, Money accountBalance) {

    public Account credit(Money amount) {
        return new Account(id, userId, accountBalance.add(amount));
    }

    public Option<Account> debit(Money amount) {
        if (accountBalance.compare(amount) < 0) {
            return Option.none();
        }
        final var newBalance = accountBalance.subtract(amount);
        return Option.of(new Account(id, userId, newBalance));
    }
}
