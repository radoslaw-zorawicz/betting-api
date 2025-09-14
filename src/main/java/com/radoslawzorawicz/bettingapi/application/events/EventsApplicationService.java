package com.radoslawzorawicz.bettingapi.application.events;

import com.radoslawzorawicz.bettingapi.domain.events.model.*;
import com.radoslawzorawicz.bettingapi.domain.events.service.OddsPolicy;
import io.vavr.control.Either;
import io.vavr.control.Option;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
public class EventsApplicationService {
    private final RaceEventsReadRepository raceEventsReadRepository;
    private final EventOutcomeRepository outcomeRepository;
    private final OddsPolicy oddsPolicy;
    private final DomainEventPublisher eventPublisher;

    public Either<RaceRetrievalError, List<Event>> getEvents(Integer year, Integer meetingKey, String sessionType) {
        return raceEventsReadRepository.getEvents(year, meetingKey, sessionType);
    }

    public Either<RaceRetrievalError, List<DriverMarket>> getDriversMarket(String sessionId) {
        return raceEventsReadRepository.getDrivers(sessionId)
                .map(drivers -> drivers.stream().map(driver -> DriverMarket.from(driver, oddsPolicy)).toList());
    }

    public Either<RaceRetrievalError, List<DriverMarket>> getDriverMarket(String sessionId, Integer driverId) {
        return raceEventsReadRepository.getDrivers(sessionId, driverId)
                .map(drivers -> drivers.stream().map(driver -> DriverMarket.from(driver, oddsPolicy)).toList());
    }

    @Transactional
    public Either<SettlementError, Void> finishEvent(String eventId, Integer winningDriverId) {
        if (eventId == null || eventId.isBlank() || winningDriverId == null || winningDriverId <= 0) {
            return Either.left(SettlementError.INVALID_REQUEST);
        }

        final OffsetDateTime dateReceived = OffsetDateTime.now();
        final Option<EventOutcome> saved = outcomeRepository.save(new EventOutcome(eventId, winningDriverId, dateReceived));
        return saved
                .toEither(SettlementError.EVENT_ALREADY_FINISHED)
                .peek(ignored -> eventPublisher.publish(new EventFinished(eventId, winningDriverId, dateReceived)))
                .map(ignored -> null);
    }
}
