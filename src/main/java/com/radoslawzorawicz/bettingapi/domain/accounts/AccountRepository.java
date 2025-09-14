package com.radoslawzorawicz.bettingapi.domain.accounts;

import io.vavr.control.Option;

import java.util.Collection;
import java.util.List;

public interface AccountRepository {
    Option<Account> findByUserId(Integer userId);

    List<Account> findAllByUserIdIn(Collection<Integer> userIds);

    Account save(Account account);

    List<Account> saveAll(Iterable<Account> accounts);
}
