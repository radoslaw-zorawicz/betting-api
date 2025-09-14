package com.radoslawzorawicz.bettingapi.application.bets;

import com.radoslawzorawicz.bettingapi.api.web.bets.PlaceBetRequest;
import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.BetPlacementError;
import io.vavr.control.Either;

import java.util.List;

public interface BetsApplicationService {
    Either<BetPlacementError, Integer> placeBet(Integer userId, PlaceBetRequest request);

    List<Bet> getBets(Integer userId);
}
