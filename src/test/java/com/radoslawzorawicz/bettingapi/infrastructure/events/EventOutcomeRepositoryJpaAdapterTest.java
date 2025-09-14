package com.radoslawzorawicz.bettingapi.infrastructure.events;

import com.radoslawzorawicz.bettingapi.domain.events.model.EventOutcome;
import io.vavr.control.Option;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

class EventOutcomeRepositoryJpaAdapterTest {

    @Test
    void shouldFindByIdAndMapToDomain() {
        // given
        final EventOutcomeJpaRepository jpa = mock(EventOutcomeJpaRepository.class);
        final EventOutcomeRepositoryJpaAdapter adapter = new EventOutcomeRepositoryJpaAdapter(jpa);

        final EventOutcomeJpaEntity entity = mock(EventOutcomeJpaEntity.class);
        given(entity.getEventId()).willReturn("E1");
        given(entity.getWinningDriverId()).willReturn(44);
        given(entity.getFinishedAt()).willReturn(OffsetDateTime.now());
        given(jpa.findById("E1")).willReturn(Optional.of(entity));

        // when
        final Option<EventOutcome> result = adapter.findById("E1");

        // then
        VavrAssertions.assertThat(result).isDefined();
        result.peek(eo -> {
            assertThat(eo.eventId()).isEqualTo("E1");
            assertThat(eo.winningDriverId()).isEqualTo(44);
        });
    }

    @Test
    void shouldReturnNoneWhenNotFound() {
        // given
        final EventOutcomeJpaRepository jpa = mock(EventOutcomeJpaRepository.class);
        given(jpa.findById("missing")).willReturn(Optional.empty());

        // when
        final EventOutcomeRepositoryJpaAdapter adapter = new EventOutcomeRepositoryJpaAdapter(jpa);

        // then
        VavrAssertions.assertThat(adapter.findById("missing")).isEmpty();
    }

    @Test
    void shouldSaveAndMapToDomain() {
        // given
        final EventOutcomeJpaRepository jpa = mock(EventOutcomeJpaRepository.class);
        final EventOutcomeRepositoryJpaAdapter adapter = new EventOutcomeRepositoryJpaAdapter(jpa);

        // when
        final EventOutcome outcome = new EventOutcome("E1", 16, OffsetDateTime.now());
        final EventOutcomeJpaEntity saved = mock(EventOutcomeJpaEntity.class);
        given(saved.getEventId()).willReturn(outcome.eventId());
        given(saved.getWinningDriverId()).willReturn(outcome.winningDriverId());
        given(saved.getFinishedAt()).willReturn(outcome.finishedAt());
        given(jpa.save(any(EventOutcomeJpaEntity.class))).willReturn(saved);

        // then
        final Option<EventOutcome> result = adapter.save(outcome);
        VavrAssertions.assertThat(result)
                .contains(new EventOutcome(outcome.eventId(), outcome.winningDriverId(), outcome.finishedAt()));
    }

    @Test
    void shouldReturnNoneOnDataIntegrityViolation() {
        // given
        final EventOutcomeJpaRepository jpa = mock(EventOutcomeJpaRepository.class);
        final EventOutcomeRepositoryJpaAdapter adapter = new EventOutcomeRepositoryJpaAdapter(jpa);

        // when
        given(jpa.save(any(EventOutcomeJpaEntity.class))).willThrow(new DataIntegrityViolationException("dup"));

        // then
        final Option<EventOutcome> result = adapter.save(new EventOutcome("E1", 1, OffsetDateTime.now()));
        VavrAssertions.assertThat(result).isEmpty();
    }

    @Test
    void shouldWrapOtherExceptions() {
        // given
        final EventOutcomeJpaRepository jpa = mock(EventOutcomeJpaRepository.class);
        final EventOutcomeRepositoryJpaAdapter adapter = new EventOutcomeRepositoryJpaAdapter(jpa);

        // when
        given(jpa.save(any(EventOutcomeJpaEntity.class))).willThrow(new RuntimeException("boom"));

        // then
        assertThatThrownBy(() -> adapter.save(new EventOutcome("E1", 1, OffsetDateTime.now())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to save EventOutcome");
    }
}
