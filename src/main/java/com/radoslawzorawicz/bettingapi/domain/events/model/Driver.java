package com.radoslawzorawicz.bettingapi.domain.events.model;

public record Driver(
        Integer driverNumber,
        String fullName,
        String teamName
) {
}
