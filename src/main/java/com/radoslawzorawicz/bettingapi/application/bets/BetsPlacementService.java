package com.radoslawzorawicz.bettingapi.application.bets;

import com.radoslawzorawicz.bettingapi.api.web.bets.PlaceBetRequest;
import com.radoslawzorawicz.bettingapi.application.events.EventsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.accounts.Account;
import com.radoslawzorawicz.bettingapi.domain.accounts.AccountRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.BetPlacementError;
import com.radoslawzorawicz.bettingapi.domain.bets.BetRepository;
import com.radoslawzorawicz.bettingapi.domain.events.model.DriverMarket;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import io.vavr.control.Either;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
public class BetsPlacementService implements BetsApplicationService {
    private final AccountRepository accountRepository;
    private final BetRepository betRepository;
    private final EventsApplicationService eventsApplicationService;


    @Transactional
    @Override
    public Either<BetPlacementError, Integer> placeBet(Integer userId, PlaceBetRequest request) {
        return eventsApplicationService.getDriverMarket(request.eventId(), request.driverId())
                .mapLeft(ignored -> BetPlacementError.INTERNAL_ERROR)
                .filterOrElse(CollectionUtils::isNotEmpty, ignored -> BetPlacementError.DRIVER_MARKET_NOT_FOUND)
                .map(List::getFirst)
                .map(DriverMarket::odds)
                .flatMap(odds -> placeBetIfSufficientBalance(userId, request, odds));
    }

    @Override
    public List<Bet> getBets(Integer userId) {
        return betRepository.findAllByUserIdOrderByIdDesc(userId);
    }

    private Either<BetPlacementError, Integer> placeBetIfSufficientBalance(Integer userId, PlaceBetRequest request, Integer odds) {
        return accountRepository.findByUserId(userId)
                .toEither(BetPlacementError.ACCOUNT_NOT_FOUND)
                .flatMap(account -> account.debit(Money.of(request.betAmount()))
                        .toEither(BetPlacementError.INSUFFICIENT_FUNDS)
                        .map(debitedAccount -> placeBet(request, odds, account, debitedAccount))
                );
    }

    private Integer placeBet(PlaceBetRequest request, Integer odds, Account account, Account debitedAccount) {
        final var bet = new Bet(
                null,
                request.eventId(),
                request.driverId(),
                account.userId(),
                Money.of(request.betAmount()),
                Bet.BetStatus.PENDING,
                odds
        );
        accountRepository.save(debitedAccount);
        return betRepository.save(bet).id();
    }
}
