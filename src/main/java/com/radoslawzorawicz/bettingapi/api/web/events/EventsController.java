package com.radoslawzorawicz.bettingapi.api.web.events;

import com.radoslawzorawicz.bettingapi.application.events.EventsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.events.model.RaceRetrievalError;
import com.radoslawzorawicz.bettingapi.domain.events.model.SettlementError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "session_type", required = false) String sessionType
    ) {
        return eventsService.getEvents(year, country, sessionType)
                .mapLeft(this::toHttpStatus)
                .fold(
                        problemDetail -> ResponseEntity.status(problemDetail.getStatus()).body(problemDetail),
                        ResponseEntity::ok
                );
    }

    @GetMapping("/{session_id}/drivers_market")
    ResponseEntity<?> getDriversMarket(@PathVariable("session_id") @NotBlank String sessionId) {
        return eventsService.getDriversMarket(sessionId)
                .mapLeft(this::toHttpStatus)
                .fold(
                        problemDetail -> ResponseEntity.status(problemDetail.getStatus()).body(problemDetail),
                        ResponseEntity::ok
                );
    }

    @PostMapping("/{event_id}/settlement")
    ResponseEntity<?> settleEvent(
            @PathVariable("event_id") @NotBlank String eventId,
            @Valid @RequestBody SettleEventRequest request
    ) {
        return eventsService.finishEvent(eventId, request.winningDriverId())
                .mapLeft(this::toHttpStatus)
                .fold(
                        problemDetail -> ResponseEntity.status(problemDetail.getStatus()).body(problemDetail),
                        ignored -> ResponseEntity.ok().build()
                );
    }

    private ProblemDetail toHttpStatus(RaceRetrievalError error) {
        final var httpStatus = switch (error) {
            case QUERY_TOO_BROAD -> HttpStatus.UNPROCESSABLE_ENTITY;
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case INTERNAL_FAILURE -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ProblemDetail.forStatusAndDetail(httpStatus, error.name());
    }

    private ProblemDetail toHttpStatus(SettlementError error) {
        final var httpStatus = switch (error) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case EVENT_ALREADY_FINISHED -> HttpStatus.CONFLICT;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ProblemDetail.forStatusAndDetail(httpStatus, error.name());
    }
}
