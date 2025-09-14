package com.radoslawzorawicz.bettingapi.infrastructure.events;

import com.radoslawzorawicz.bettingapi.domain.events.model.EventOutcome;
import com.radoslawzorawicz.bettingapi.domain.events.model.EventOutcomeRepository;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.dao.DataIntegrityViolationException;

class EventOutcomeRepositoryJpaAdapter implements EventOutcomeRepository {

    private final EventOutcomeJpaRepository jpaRepository;

    EventOutcomeRepositoryJpaAdapter(EventOutcomeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Option<EventOutcome> findById(String eventId) {
        return Option.ofOptional(jpaRepository.findById(eventId))
                .map(this::toDomainEntity);
    }

    @Override
    public Option<EventOutcome> save(EventOutcome outcome) {
        final EventOutcomeJpaEntity jpaEntity = toJpaEntity(outcome);
        return Try.of(() -> jpaRepository.save(jpaEntity))
                .map(this::toDomainEntity)
                .map(Option::of)
                .recover(DataIntegrityViolationException.class, ignored -> Option.none())
                .getOrElseThrow(thr -> new IllegalStateException("Failed to save EventOutcome", thr));
    }

    private EventOutcome toDomainEntity(EventOutcomeJpaEntity outcomeJpaEntity) {
        return new EventOutcome(outcomeJpaEntity.getEventId(), outcomeJpaEntity.getWinningDriverId(), outcomeJpaEntity.getFinishedAt());
    }

    private EventOutcomeJpaEntity toJpaEntity(EventOutcome d) {
        return new EventOutcomeJpaEntity(d.eventId(), d.winningDriverId(), d.finishedAt());
    }
}
