package com.radoslawzorawicz.bettingapi.application.bets;

import com.radoslawzorawicz.bettingapi.api.web.bets.PlaceBetRequest;
import com.radoslawzorawicz.bettingapi.application.events.EventsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.accounts.Account;
import com.radoslawzorawicz.bettingapi.domain.accounts.AccountRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.BetPlacementError;
import com.radoslawzorawicz.bettingapi.domain.bets.BetRepository;
import com.radoslawzorawicz.bettingapi.domain.events.model.Driver;
import com.radoslawzorawicz.bettingapi.domain.events.model.DriverMarket;
import com.radoslawzorawicz.bettingapi.domain.events.model.RaceRetrievalError;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.assertj.vavr.api.VavrAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetsPlacementServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private BetRepository betRepository;
    @Mock
    private EventsApplicationService events;

    @InjectMocks
    private BetsPlacementService service;

    @AfterEach
    void resetMocks() {
        reset(accountRepository, betRepository, events);
    }

    @Test
    void shouldPlaceBetAndDebitAccount() {
        // given
        final var userId = 7;
        final var request = new PlaceBetRequest("EVT", 44, new BigDecimal("10.00"));

        final var driver = new Driver(44, "N", "T");
        final var driverMarket = new DriverMarket(driver, 3);
        given(events.getDriverMarket("EVT", 44)).willReturn(Either.right(List.of(driverMarket)));

        final var userAccount = new Account(1, userId, Money.of(new BigDecimal("50.00")));
        given(accountRepository.findByUserId(userId)).willReturn(Option.of(userAccount));
        given(betRepository.save(any(Bet.class)))
                .willAnswer(savedBetWithId(99));

        // when
        final var result = service.placeBet(userId, request);

        // then
        VavrAssertions.assertThat(result).containsOnRight(99);
        verify(accountRepository).save(new Account(1, userId, Money.of(new BigDecimal("40.00"))));
        verify(betRepository).save(new Bet(null, "EVT", 44, userId, Money.of(new BigDecimal("10.00")), Bet.BetStatus.PENDING, 3));
    }

    @Test
    void shouldFailWhenInsufficientFunds() {
        // given
        final var userId = 7;
        final var request = new PlaceBetRequest("EVT", 44, new BigDecimal("10.00"));

        final var driver = new Driver(44, "N", "T");
        final var driverMarket = new DriverMarket(driver, 2);
        given(events.getDriverMarket("EVT", 44)).willReturn(Either.right(List.of(driverMarket)));

        final var userAccount = new Account(1, userId, Money.of(new BigDecimal("5.00")));
        given(accountRepository.findByUserId(userId)).willReturn(Option.of(userAccount));

        // when
        final var result = service.placeBet(userId, request);

        // then
        VavrAssertions.assertThat(result).containsOnLeft(BetPlacementError.INSUFFICIENT_FUNDS);
        verify(betRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldReturnAccountNotFoundWhenMissing() {
        // given
        final var userId = 77;
        final var request = new PlaceBetRequest("EVT", 16, new BigDecimal("5.00"));

        final var driver = new Driver(16, "N", "T");
        final var driverMarket = new DriverMarket(driver, 2);
        given(events.getDriverMarket("EVT", 16)).willReturn(Either.right(List.of(driverMarket)));
        given(accountRepository.findByUserId(userId)).willReturn(Option.none());

        // when
        final var result = service.placeBet(userId, request);

        // then
        VavrAssertions.assertThat(result).containsOnLeft(BetPlacementError.ACCOUNT_NOT_FOUND);
        verifyNoInteractions(betRepository);
    }

    @Test
    void shouldReturnDriverMarketNotFoundWhenEmpty() {
        // given
        final var userId = 7;
        final var request = new PlaceBetRequest("EVT", 44, new BigDecimal("10.00"));

        given(events.getDriverMarket("EVT", 44)).willReturn(Either.right(List.of()));

        // when
        final var result = service.placeBet(userId, request);

        // then
        VavrAssertions.assertThat(result).containsOnLeft(BetPlacementError.DRIVER_MARKET_NOT_FOUND);
        verifyNoInteractions(accountRepository, betRepository);
    }

    @Test
    void shouldMapInternalErrorWhenDriverMarketFails() {
        // given
        final var userId = 7;
        final var request = new PlaceBetRequest("EVT", 99, new BigDecimal("10.00"));

        given(events.getDriverMarket("EVT", 99)).willReturn(Either.left(RaceRetrievalError.INTERNAL_FAILURE));

        // when
        final var result = service.placeBet(userId, request);

        // then
        VavrAssertions.assertThat(result).containsOnLeft(BetPlacementError.INTERNAL_ERROR);
        verifyNoInteractions(accountRepository, betRepository);
    }

    @Test
    void shouldUseFirstDriverMarketEntryWhenMultipleReturned() {
        // given
        final int userId = 5;
        final var request = new PlaceBetRequest("EVT", 44, new BigDecimal("10.00"));

        final var driverMarket1 = new DriverMarket(new Driver(44, "Lewis Hamilton", "Mercedes"), 5);
        final var driverMarket2 = new DriverMarket(new Driver(44, "Lewis Hamilton", "Mercedes"), 7);
        given(events.getDriverMarket("EVT", 44)).willReturn(Either.right(List.of(driverMarket1, driverMarket2)));

        final var userAccount = new Account(1, userId, Money.of(new BigDecimal("100.00")));
        given(accountRepository.findByUserId(userId)).willReturn(Option.of(userAccount));

        given(betRepository.save(any(Bet.class))).willAnswer(savedBetWithId(123));

        // when
        final Either<BetPlacementError, Integer> result = service.placeBet(userId, request);

        // then
        final ArgumentCaptor<Bet> betCaptor = ArgumentCaptor.forClass(Bet.class);

        VavrAssertions.assertThat(result).containsOnRight(123);
        verify(betRepository).save(betCaptor.capture());
        assertThat(betCaptor.getValue().odds()).isEqualTo(5);
    }

    @Test
    void shouldDebitAndPersistAccountBeforeSavingBet() {
        // given
        final int userId = 9;
        final var request = new PlaceBetRequest("EVT", 44, new BigDecimal("10.00"));

        final var driver = new Driver(44, "N", "T");
        final var driverMarket = new DriverMarket(driver, 3);
        given(events.getDriverMarket("EVT", 44)).willReturn(Either.right(List.of(driverMarket)));

        final var userAccount = new Account(1, userId, Money.of(new BigDecimal("50.00")));
        given(accountRepository.findByUserId(userId)).willReturn(Option.of(userAccount));

        given(betRepository.save(any(Bet.class))).willAnswer(savedBetWithId(200));

        // when
        service.placeBet(userId, request);

        // then
        final Account expectedDebited = new Account(1, userId, Money.of(new BigDecimal("40.00")));
        final Bet expectedBet = new Bet(null, "EVT", 44, userId, Money.of(new BigDecimal("10.00")), Bet.BetStatus.PENDING, 3);
        final InOrder inOrder = inOrder(accountRepository, betRepository);
        inOrder.verify(accountRepository).save(expectedDebited);
        inOrder.verify(betRepository).save(expectedBet);
    }

    private Answer<Bet> savedBetWithId(int id) {
        return inv -> ((Bet) inv.getArgument(0)).withId(id);
    }
}
