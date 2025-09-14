package com.radoslawzorawicz.bettingapi.domain.events.model;

import io.vavr.control.Either;

import java.util.List;

public interface RaceEventsReadRepository {
    Either<RaceRetrievalError, List<Event>> getEvents(Integer year, String country, String sessionType);

    Either<RaceRetrievalError, List<Driver>>  getDrivers(String sessionKey, Integer driverId);

    Either<RaceRetrievalError, List<Driver>> getDrivers(String sessionKey);
}
