package com.radoslawzorawicz.bettingapi.application.accounts;

import com.radoslawzorawicz.bettingapi.domain.events.model.EventFinished;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EventFinishedTransactionalHandlerTest {

    @Mock
    private BetsSettlementService betsSettlementService;

    @InjectMocks
    private EventFinishedTransactionalHandler handler;

    @Test
    void shouldCallSettleWithEventData() {
        // given
        final var event = new EventFinished("EVT-2", 16, OffsetDateTime.parse("2024-01-01T00:00:00Z"));

        // when
        handler.handle(event);

        // then
        then(betsSettlementService).should().settle("EVT-2", 16);
    }
}

