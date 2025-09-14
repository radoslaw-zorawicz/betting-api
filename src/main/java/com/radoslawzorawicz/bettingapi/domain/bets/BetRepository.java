package com.radoslawzorawicz.bettingapi.domain.bets;

import java.util.List;

public interface BetRepository {
    Bet save(Bet bet);

    void saveAll(List<Bet> bets);

    List<Bet> findAllByUserIdOrderByIdDesc(Integer userId);

    List<Bet> findAllByEventIdAndStatus(String eventId, Bet.BetStatus status);
}
