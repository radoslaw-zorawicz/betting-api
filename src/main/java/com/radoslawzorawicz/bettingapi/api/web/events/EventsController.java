package com.radoslawzorawicz.bettingapi.api.web.events;

import com.radoslawzorawicz.bettingapi.application.events.EventsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.events.model.RaceRetrievalError;
import com.radoslawzorawicz.bettingapi.domain.events.model.SettlementError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/events")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Validated
class EventsController {
    private final EventsApplicationService eventsService;

    @GetMapping
    ResponseEntity<?> getEvents(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "meeting_key", required = false) Integer meetingKey,
            @RequestParam(value = "session_type", required = false) String sessionType
    ) {
        return eventsService.getEvents(year, meetingKey, sessionType)
                .mapLeft(this::toHttpStatus)
                .fold(status -> ResponseEntity.status(status).build(), ResponseEntity::ok);
    }

    @GetMapping("/{session_id}/drivers_market")
    ResponseEntity<?> getDriversMarket(@PathVariable("session_id") @NotBlank String sessionId) {
        return eventsService.getDriversMarket(sessionId)
                .mapLeft(this::toHttpStatus)
                .fold(status -> ResponseEntity.status(status).build(), ResponseEntity::ok);
    }

    @PostMapping("/{event_id}/settlement")
    ResponseEntity<?> settleEvent(
            @PathVariable("event_id") @NotBlank String eventId,
            @Valid @RequestBody SettleEventRequest request
    ) {
        return eventsService.finishEvent(eventId, request.winningDriverId())
                .mapLeft(this::toHttpStatus)
                .fold(status -> ResponseEntity.status(status).build(), ignored -> ResponseEntity.ok().build());
    }

    private HttpStatus toHttpStatus(RaceRetrievalError error) {
        return switch (error) {
            case QUERY_TOO_BROAD -> HttpStatus.UNPROCESSABLE_ENTITY;
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case INTERNAL_FAILURE -> HttpStatus.BAD_GATEWAY;
        };
    }

    private HttpStatus toHttpStatus(SettlementError error) {
        return switch (error) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case EVENT_ALREADY_FINISHED -> HttpStatus.CONFLICT;
            case INTERNAL_ERROR -> HttpStatus.BAD_GATEWAY;
        };
    }
}
