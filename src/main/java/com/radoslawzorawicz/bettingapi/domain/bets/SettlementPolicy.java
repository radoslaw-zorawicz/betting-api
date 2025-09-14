package com.radoslawzorawicz.bettingapi.domain.bets;

import com.radoslawzorawicz.bettingapi.domain.shared.Money;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public final class SettlementPolicy {

    public List<Bet> resolve(List<Bet> pendingBets, Integer winningDriverId) {
        return pendingBets.stream()
                .map(bet -> bet.resolveByDriverId(winningDriverId))
                .toList();
    }

    public Map<Integer, Money> creditsByUser(List<Bet> bets) {
        return bets.stream()
                .filter(Bet::isWon)
                .collect(groupingBy(
                        Bet::userId,
                        mapping(Bet::calculatePayout, reducing(Money.zero(), Money::add))
                ));
    }
}
