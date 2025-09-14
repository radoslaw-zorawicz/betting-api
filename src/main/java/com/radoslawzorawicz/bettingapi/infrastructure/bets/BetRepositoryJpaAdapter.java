package com.radoslawzorawicz.bettingapi.infrastructure.bets;

import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.BetRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class BetRepositoryJpaAdapter implements BetRepository {

    private final BetJpaRepository jpaRepository;
    private final BetMapper betMapper;

    @Override
    public Bet save(Bet bet) {
        final var saved = jpaRepository.save(betMapper.toEntity(bet));
        return betMapper.toDomain(saved);
    }

    @Override
    public void saveAll(List<Bet> bets) {
        final List<BetJpaEntity> betJpaEntities = bets.stream()
                .map(betMapper::toEntity)
                .toList();
        jpaRepository.saveAll(betJpaEntities);
    }

    @Override
    public List<Bet> findAllByUserIdOrderByIdDesc(Integer userId) {
        return jpaRepository.findAllByUserIdOrderByIdDesc(userId).stream().map(betMapper::toDomain).toList();
    }

    @Override
    public List<Bet> findAllByEventIdAndStatus(String eventId, Bet.BetStatus status) {
        return jpaRepository.findAllByEventIdAndStatus(eventId, status).stream().map(betMapper::toDomain).toList();
    }
}
