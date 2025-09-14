package com.radoslawzorawicz.bettingapi.application.accounts;

import com.radoslawzorawicz.bettingapi.domain.accounts.Account;
import com.radoslawzorawicz.bettingapi.domain.accounts.AccountRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.BetRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.SettlementPolicy;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetsSettlementServiceTest {

    @Mock
    private BetRepository betRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private SettlementPolicy settlementPolicy;
    @InjectMocks
    private BetsSettlementService service;

    @AfterEach
    void resetMocks() {
        reset(betRepository, accountRepository, settlementPolicy);
    }

    @Test
    void shouldHandleMultipleWinningBetsPerUserByAggregatingPayouts() {
        final var eventId = "EVT-1";
        final var winningDriverId = 44;
        final var userId = 101;

        final var bet1 = new Bet(1, eventId, winningDriverId, userId, Money.of(new BigDecimal("10.00")), Bet.BetStatus.PENDING, 2); // payout 20
        final var bet2 = new Bet(2, eventId, winningDriverId, userId, Money.of(new BigDecimal("20.00")), Bet.BetStatus.PENDING, 3); // payout 60

        given(betRepository.findAllByEventIdAndStatus(eventId, Bet.BetStatus.PENDING))
                .willReturn(List.of(bet1, bet2));
        given(settlementPolicy.resolve(anyList(), eq(winningDriverId)))
                .willReturn(List.of(
                        new Bet(1, eventId, winningDriverId, userId, Money.of(new BigDecimal("10.00")), Bet.BetStatus.WON, 2),
                        new Bet(2, eventId, winningDriverId, userId, Money.of(new BigDecimal("20.00")), Bet.BetStatus.WON, 3)
                ));
        given(settlementPolicy.creditsByUser(anyList()))
                .willReturn(java.util.Map.of(userId, Money.of(new BigDecimal("80.00"))));

        given(accountRepository.findAllByUserIdIn(any()))
                .willReturn(List.of(new Account(5, userId, Money.of(new BigDecimal("100.00")))));

        service.settle(eventId, winningDriverId);

        final var expectedAccountSave = new Account(5, userId, Money.of(new BigDecimal("180.00")));
        verify(accountRepository).saveAll(List.of(expectedAccountSave));
    }

    @Test
    void shouldDoNothingWhenNoPendingBets() {
        // given
        final var eventId = "EVT-EMPTY";
        final var winningDriverId = 99;
        given(betRepository.findAllByEventIdAndStatus(eventId, Bet.BetStatus.PENDING))
                .willReturn(List.of());

        // when
        service.settle(eventId, winningDriverId);

        // then
        then(settlementPolicy).should(never()).resolve(anyList(), anyInt());
        then(betRepository).should(never()).saveAll(anyList());
        then(accountRepository).should(never()).findAllByUserIdIn(any());
        then(accountRepository).should(never()).saveAll(anyList());
    }

    @Test
    void shouldSaveLostStatusesAndNotCreditAnyAccountsWhenNoWinners() {
        // given
        final var eventId = "EVT-NO-WINNERS";
        final var winningDriverId = 1;
        final var bet1 = new Bet(1, eventId, 44, 7, Money.of(new BigDecimal("5.00")), Bet.BetStatus.PENDING, 2);
        final var bet2 = new Bet(2, eventId, 16, 8, Money.of(new BigDecimal("3.00")), Bet.BetStatus.PENDING, 4);

        given(betRepository.findAllByEventIdAndStatus(eventId, Bet.BetStatus.PENDING))
                .willReturn(List.of(bet1, bet2));
        final var resolved = List.of(
                new Bet(1, eventId, 44, 7, Money.of(new BigDecimal("5.00")), Bet.BetStatus.LOST, 2),
                new Bet(2, eventId, 16, 8, Money.of(new BigDecimal("3.00")), Bet.BetStatus.LOST, 4)
        );
        given(settlementPolicy.resolve(anyList(), eq(winningDriverId))).willReturn(resolved);
        given(settlementPolicy.creditsByUser(resolved)).willReturn(java.util.Map.of());
        given(accountRepository.findAllByUserIdIn(any())).willReturn(List.of());

        // when
        service.settle(eventId, winningDriverId);

        // then
        then(betRepository).should().saveAll(resolved);
        then(accountRepository).should().findAllByUserIdIn(Set.of());
        then(accountRepository).should().saveAll(List.of());
    }

    @Test
    void shouldCreditMultipleWinnerAccountsAcrossUsers() {
        // given
        final var eventId = "EVT-MULTI";
        final var winningDriverId = 10;

        final var betU1a = new Bet(1, eventId, winningDriverId, 101, Money.of(new BigDecimal("5.00")), Bet.BetStatus.PENDING, 2); // 10
        final var betU1b = new Bet(2, eventId, winningDriverId, 101, Money.of(new BigDecimal("3.00")), Bet.BetStatus.PENDING, 5); // 15
        final var betU2a = new Bet(3, eventId, winningDriverId, 202, Money.of(new BigDecimal("4.00")), Bet.BetStatus.PENDING, 3); // 12

        given(betRepository.findAllByEventIdAndStatus(eventId, Bet.BetStatus.PENDING))
                .willReturn(List.of(betU1a, betU1b, betU2a));

        final var resolved = List.of(
                new Bet(1, eventId, winningDriverId, 101, Money.of(new BigDecimal("5.00")), Bet.BetStatus.WON, 2),
                new Bet(2, eventId, winningDriverId, 101, Money.of(new BigDecimal("3.00")), Bet.BetStatus.WON, 5),
                new Bet(3, eventId, winningDriverId, 202, Money.of(new BigDecimal("4.00")), Bet.BetStatus.WON, 3)
        );
        given(settlementPolicy.resolve(anyList(), eq(winningDriverId))).willReturn(resolved);
        given(settlementPolicy.creditsByUser(resolved)).willReturn(java.util.Map.of(
                101, Money.of(new BigDecimal("25.00")),
                202, Money.of(new BigDecimal("12.00"))
        ));

        given(accountRepository.findAllByUserIdIn(Set.of(101, 202)))
                .willReturn(List.of(
                        new Account(11, 101, Money.of(new BigDecimal("100.00"))),
                        new Account(22, 202, Money.of(new BigDecimal("50.00")))
                ));

        // when
        service.settle(eventId, winningDriverId);

        // then
        then(betRepository).should().saveAll(resolved);
        then(accountRepository).should().findAllByUserIdIn(Set.of(101, 202));
        then(accountRepository).should().saveAll(List.of(
                new Account(11, 101, Money.of(new BigDecimal("125.00"))),
                new Account(22, 202, Money.of(new BigDecimal("62.00")))
        ));
    }
}
