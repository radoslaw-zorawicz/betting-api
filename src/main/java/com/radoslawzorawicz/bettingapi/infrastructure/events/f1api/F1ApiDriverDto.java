package com.radoslawzorawicz.bettingapi.infrastructure.events.f1api;

import com.radoslawzorawicz.bettingapi.domain.events.model.Driver;

record F1ApiDriverDto(
        Integer driverNumber,
        String fullName,
        String teamName
) {
    Driver toDriver() {
        return new Driver(driverNumber, fullName, teamName);
    }
}
