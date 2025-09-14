package com.radoslawzorawicz.bettingapi.infrastructure.events;

import com.radoslawzorawicz.bettingapi.domain.events.model.EventFinished;
import com.radoslawzorawicz.bettingapi.domain.events.model.EventFinishedHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class InMemoryDomainEventPublisherTest {

    @Mock
    private EventFinishedHandler handler;

    @Test
    void shouldForwardEventToHandler() {
        // given
        final var publisher = new InMemoryDomainEventPublisher(handler);
        final var event = new EventFinished("EVT-1", 44, OffsetDateTime.parse("2024-01-01T00:00:00Z"));

        // when
        publisher.publish(event);

        // then
        then(handler).should().handle(event);
    }
}

