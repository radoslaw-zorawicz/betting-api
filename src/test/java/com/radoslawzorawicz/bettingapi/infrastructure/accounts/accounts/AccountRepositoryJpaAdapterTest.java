package com.radoslawzorawicz.bettingapi.infrastructure.accounts.accounts;

import com.radoslawzorawicz.bettingapi.domain.accounts.Account;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import com.radoslawzorawicz.bettingapi.infrastructure.accounts.AccountJpaEntity;
import com.radoslawzorawicz.bettingapi.infrastructure.accounts.AccountJpaRepository;
import com.radoslawzorawicz.bettingapi.infrastructure.accounts.AccountRepositoryJpaAdapter;
import io.vavr.control.Option;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountRepositoryJpaAdapterTest {

    @Mock
    private AccountJpaRepository jpa;
    @InjectMocks
    private AccountRepositoryJpaAdapter adapter;

    @AfterEach
    void resetMocks() {
        reset(jpa);
    }

    @Test
    void shouldFindByUserIdAndMapToDomain() {
        // given
        given(jpa.findByUserId(5)).willReturn(Optional.of(new AccountJpaEntity(1, 5, new BigDecimal("10.00"))));

        // when
        final Option<Account> result = adapter.findByUserId(5);

        // then
        org.assertj.vavr.api.VavrAssertions.assertThat(result)
                .contains(new Account(1, 5, Money.of(new BigDecimal("10.00"))));
    }

    @Test
    void shouldFindAllByUserIds() {
        given(jpa.findAllByUserIdIn(any())).willReturn(List.of(
                new AccountJpaEntity(1, 5, new BigDecimal("10.00")),
                new AccountJpaEntity(2, 6, new BigDecimal("20.00"))
        ));

        final List<Account> accounts = adapter.findAllByUserIdIn(List.of(5, 6));
        final var expected = List.of(
                new Account(1, 5, Money.of(new BigDecimal("10.00"))),
                new Account(2, 6, Money.of(new BigDecimal("20.00")))
        );
        assertThat(accounts).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void shouldSaveAndMapBack() {
        final var domain = new Account(10, 77, Money.of(new BigDecimal("15.00")));
        given(jpa.save(any(AccountJpaEntity.class))).willAnswer(inv -> inv.getArgument(0));

        final Account saved = adapter.save(domain);
        assertThat(saved).isEqualTo(domain);

        final ArgumentCaptor<AccountJpaEntity> captor = ArgumentCaptor.forClass(AccountJpaEntity.class);
        verify(jpa).save(captor.capture());
        final var expectedEntity = new AccountJpaEntity(10, 77, new BigDecimal("15.00"));
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedEntity);
    }

    @Test
    void shouldSaveAllAndMapBack() {
        final var a1 = new Account(1, 5, Money.of(new BigDecimal("10.00")));
        final var a2 = new Account(2, 6, Money.of(new BigDecimal("20.00")));

        given(jpa.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        final List<Account> saved = adapter.saveAll(List.of(a1, a2));
        assertThat(saved)
                .hasSize(2)
                .containsExactly(a1, a2);
    }
}
