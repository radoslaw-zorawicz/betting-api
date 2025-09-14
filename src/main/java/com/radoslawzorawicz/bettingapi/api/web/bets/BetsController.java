package com.radoslawzorawicz.bettingapi.api.web.bets;

import com.radoslawzorawicz.bettingapi.application.bets.BetsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.bets.BetPlacementError;
import com.radoslawzorawicz.bettingapi.domain.bets.PlaceBetCommand;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bets")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Validated
class BetsController {
    private final BetsApplicationService betsService;

    @GetMapping
    ResponseEntity<?> getBets(@RequestHeader("X-USER-ID") Integer userId) {
        final var bets = betsService.getBets(userId)
                .stream()
                .map(BetDto::toDto)
                .toList();
        return ResponseEntity.ok(bets);
    }

    @PostMapping
    ResponseEntity<?> placeBet(
            @RequestHeader("X-USER-ID") Integer userId,
            @Valid @RequestBody PlaceBetRequest request
    ) {
        final var placeBetCommand = new PlaceBetCommand(request.eventId(), request.driverId(), request.betAmount());
        return betsService.placeBet(userId, placeBetCommand)
                .mapLeft(this::errorToHttpStatus)
                .map(BetCreatedDto::new)
                .fold(
                        problemDetail -> ResponseEntity.status(problemDetail.getStatus()).body(problemDetail),
                        body -> ResponseEntity.status(HttpStatus.CREATED).body(body)
                );
    }

    private ProblemDetail errorToHttpStatus(BetPlacementError error) {
        final var httpStatus = switch (error) {
            case ACCOUNT_NOT_FOUND, DRIVER_MARKET_NOT_FOUND -> HttpStatus.BAD_REQUEST;
            case INSUFFICIENT_FUNDS -> HttpStatus.PAYMENT_REQUIRED;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ProblemDetail.forStatusAndDetail(httpStatus, error.name());
    }
}
