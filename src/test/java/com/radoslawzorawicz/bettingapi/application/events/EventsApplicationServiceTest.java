package com.radoslawzorawicz.bettingapi.application.events;

import com.radoslawzorawicz.bettingapi.domain.events.model.*;
import com.radoslawzorawicz.bettingapi.domain.events.service.OddsPolicy;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventsApplicationServiceTest {

    @Mock
    private RaceEventsReadRepository readRepo;
    @Mock
    private EventOutcomeRepository outcomeRepo;
    @Mock
    private DomainEventPublisher publisher;
    @Mock
    private OddsPolicy oddsPolicy;

    @InjectMocks
    private EventsApplicationService service;

    @AfterEach
    void resetMocks() {
        reset(readRepo, outcomeRepo, publisher, oddsPolicy);
    }

    @Test
    void shouldMapDriversToMarketUsingOddsPolicy() {
        // given
        final String sessionId = "S1";
        given(readRepo.getDrivers(sessionId)).willReturn(Either.right(List.of(
                new Driver(44, "Lewis Hamilton", "Mercedes"),
                new Driver(1, "Max Verstappen", "Red Bull")
        )));
        given(oddsPolicy.nextOdds()).willReturn(Odds.of(3));

        // when
        final Either<RaceRetrievalError, List<DriverMarket>> result = service.getDriversMarket(sessionId);

        // then
        assertThat(result)
                .hasRightValueSatisfying(market -> Assertions.assertThat(market)
                        .hasSize(2)
                        .allMatch(driverMarkets -> driverMarkets.odds() == 3)
                        .extracting(dm -> dm.driver().driverNumber())
                        .containsExactly(44, 1)
                );

        verifyNoInteractions(outcomeRepo, publisher);
    }

    @Test
    void shouldReturnInvalidRequestOnFinishEventBadInput() {
        // when
        final Either<SettlementError, Void> result = service.finishEvent(" ", -1);

        // then
        assertThat(result).containsOnLeft(SettlementError.INVALID_REQUEST);
    }

    @Test
    void shouldPublishEventOnSuccessfulFinish() {
        // given
        final String eventId = "E1";
        final Integer winner = 16;
        given(outcomeRepo.save(any(EventOutcome.class))).willReturn(Option.of(new EventOutcome(eventId, winner, OffsetDateTime.now())));

        // when
        final Either<SettlementError, Void> result = service.finishEvent(eventId, winner);

        // then
        assertThat(result).isRight();
        verify(publisher).publish(any(EventFinished.class));
    }

    @Test
    void shouldReturnAlreadyFinishedWhenOutcomeExists() {
        // given
        given(outcomeRepo.save(any(EventOutcome.class))).willReturn(Option.none());

        // when
        final Either<SettlementError, Void> result = service.finishEvent("E1", 44);

        // then
        assertThat(result).containsOnLeft(SettlementError.EVENT_ALREADY_FINISHED);
    }

    @Test
    void shouldMapDriverSpecificMarketUsingOddsPolicy() {
        // given
        final String sessionId = "S1";
        final Integer driverId = 44;
        given(readRepo.getDrivers(sessionId, driverId)).willReturn(Either.right(List.of(
                new Driver(44, "Lewis Hamilton", "Mercedes")
        )));
        given(oddsPolicy.nextOdds()).willReturn(Odds.of(4));

        // when
        final Either<RaceRetrievalError, List<DriverMarket>> result = service.getDriverMarket(sessionId, driverId);

        // then
        assertThat(result)
                .hasRightValueSatisfying(market -> Assertions.assertThat(market)
                        .containsExactly(new DriverMarket(new Driver(44, "Lewis Hamilton", "Mercedes"), 4))
                );
    }

    @Test
    void shouldPropagateErrorFromGetDriversMarket() {
        // given
        final String sessionId = "S_ERR";
        given(readRepo.getDrivers(sessionId)).willReturn(Either.left(RaceRetrievalError.RATE_LIMITED));

        // when
        final Either<RaceRetrievalError, List<DriverMarket>> result = service.getDriversMarket(sessionId);

        // then
        assertThat(result).containsOnLeft(RaceRetrievalError.RATE_LIMITED);
        verifyNoInteractions(outcomeRepo, publisher, oddsPolicy);
    }

    @Test
    void shouldPropagateErrorFromGetDriverMarket() {
        // given
        final String sessionId = "S_ERR";
        final Integer driverId = 99;
        given(readRepo.getDrivers(sessionId, driverId)).willReturn(Either.left(RaceRetrievalError.INTERNAL_FAILURE));

        // when
        final Either<RaceRetrievalError, List<DriverMarket>> result = service.getDriverMarket(sessionId, driverId);

        // then
        assertThat(result).containsOnLeft(RaceRetrievalError.INTERNAL_FAILURE);
        verifyNoInteractions(outcomeRepo, publisher, oddsPolicy);
    }

    @Test
    void shouldReturnInvalidRequestOnFinishEventNullEventId() {
        // when
        final Either<SettlementError, Void> result = service.finishEvent(null, 44);

        // then
        assertThat(result).containsOnLeft(SettlementError.INVALID_REQUEST);
    }

    @Test
    void shouldReturnInvalidRequestOnFinishEventNullWinningDriver() {
        // when
        final Either<SettlementError, Void> result = service.finishEvent("E1", null);

        // then
        assertThat(result).containsOnLeft(SettlementError.INVALID_REQUEST);
    }
}
