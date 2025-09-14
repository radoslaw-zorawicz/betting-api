package com.radoslawzorawicz.bettingapi.application.bets;

import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.BetPlacementError;
import com.radoslawzorawicz.bettingapi.domain.bets.PlaceBetCommand;
import io.vavr.control.Either;

import java.util.List;

public interface BetsApplicationService {
    Either<BetPlacementError, Integer> placeBet(Integer userId, PlaceBetCommand request);

    List<Bet> getBets(Integer userId);
}
