package com.radoslawzorawicz.bettingapi.api.web.bets;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PlaceBetRequest(
        @NotBlank
        String eventId,

        @NotNull
        @Positive
        Integer driverId,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 12, fraction = 2)
        BigDecimal betAmount
) {
}
