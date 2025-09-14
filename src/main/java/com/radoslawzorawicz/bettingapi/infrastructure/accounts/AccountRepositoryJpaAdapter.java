package com.radoslawzorawicz.bettingapi.infrastructure.accounts;

import com.radoslawzorawicz.bettingapi.domain.accounts.Account;
import com.radoslawzorawicz.bettingapi.domain.accounts.AccountRepository;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AccountRepositoryJpaAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;

    @Override
    public Option<Account> findByUserId(Integer userId) {
        return Option.ofOptional(jpaRepository.findByUserId(userId)).map(this::toDomain);
    }

    @Override
    public List<Account> findAllByUserIdIn(Collection<Integer> userIds) {
        return jpaRepository.findAllByUserIdIn(userIds).stream().map(this::toDomain).toList();
    }

    @Override
    public Account save(Account account) {
        return toDomain(jpaRepository.save(toEntity(account)));
    }

    @Override
    public List<Account> saveAll(Iterable<Account> accounts) {
        final List<AccountJpaEntity> entities = java.util.stream.StreamSupport.stream(accounts.spliterator(), false)
                .map(this::toEntity)
                .toList();

        return jpaRepository.saveAll(entities)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Account toDomain(AccountJpaEntity accountJpaEntity) {
        return new Account(
                accountJpaEntity.getId(),
                accountJpaEntity.getUserId(),
                Money.of(accountJpaEntity.getAccountBalance())
        );
    }

    private AccountJpaEntity toEntity(Account account) {
        return new AccountJpaEntity(
                account.id(),
                account.userId(),
                account.accountBalance().toBigDecimal()
        );
    }
}
