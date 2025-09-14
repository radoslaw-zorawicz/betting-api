package com.radoslawzorawicz.bettingapi.infrastructure.bets;

import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetJpaRepository extends JpaRepository<BetJpaEntity, Integer> {
    List<BetJpaEntity> findAllByUserIdOrderByIdDesc(Integer userId);

    List<BetJpaEntity> findAllByEventIdAndStatus(String eventId, Bet.BetStatus status);
}
