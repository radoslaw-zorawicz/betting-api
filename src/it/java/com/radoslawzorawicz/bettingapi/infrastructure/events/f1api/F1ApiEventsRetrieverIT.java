package com.radoslawzorawicz.bettingapi.infrastructure.events.f1api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.radoslawzorawicz.bettingapi.domain.events.model.Driver;
import com.radoslawzorawicz.bettingapi.domain.events.model.Event;
import com.radoslawzorawicz.bettingapi.domain.events.model.RaceRetrievalError;
import io.vavr.control.Either;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.*;

class F1ApiEventsRetrieverIT {

    private static WireMockServer f1ApiMock;

    @BeforeAll
    static void start() {
        f1ApiMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        f1ApiMock.start();
        configureFor("localhost", f1ApiMock.port());
    }

    @AfterAll
    static void stop() {
        if (f1ApiMock != null) f1ApiMock.stop();
    }

    @AfterEach
    void restServer() {
        f1ApiMock.resetAll();
    }

    private F1ApiEventsRetriever retriever(int maxAttempts) {
        final var props = new OpenF1Properties(
                f1ApiMock.baseUrl(),
                new OpenF1Properties.Rate(100, 1),
                new OpenF1Properties.Retry(maxAttempts, 1, 2.0, 0.0, 10)
        );
        return new F1ApiEventsRetriever(RestClient.builder(), props);
    }

    @Test
    void shouldMapSessionsDtoToEvents() {
        // given
        final String now = "2024-01-01T00:00:00Z";
        f1ApiMock.stubFor(get(urlPathEqualTo("/sessions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(("""
                                [
                                  {
                                    "sessionKey": "S1",
                                    "sessionName": "Race 1",
                                    "sessionType": "R",
                                    "year": 2024,
                                    "country": "UK",
                                    "dateStart": "%s",
                                    "dateEnd": "%s"
                                  }
                                ]
                                """.formatted(now, now))
                        )));

        // when
        final Either<RaceRetrievalError, List<Event>> result = retriever(1).getEvents(2024, null, "R");

        // then
        assertThat(result).containsOnRight(List.of(
                new Event("S1", "Race 1", "R", 2024, "UK", OffsetDateTime.parse(now), OffsetDateTime.parse(now))
        ));
        verify(getRequestedFor(urlPathEqualTo("/sessions")).withQueryParam("session_type", equalTo("R")));
    }

    private static Stream<Arguments> getEventsByFilterCase() {
        return Stream.of(
                arguments("country_name", "UK", "UK", null, null),
                arguments("session_type", "Race", null, "Race", null),
                arguments("year", "2024", null, null, 2024)
        );
    }

    @ParameterizedTest
    @MethodSource("getEventsByFilterCase")
    void shouldFilterEventsByCountry(
            String testedParamName,
            String testedParamValue,
            String givenCountry,
            String givenSessionType,
            Integer givenYear
    ) {
        // given
        final String now = "2024-01-01T00:00:00Z";
        f1ApiMock.stubFor(get(urlPathEqualTo("/sessions"))
                .withQueryParam(testedParamName, equalTo(testedParamValue))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(("""
                                [
                                  {
                                    "sessionKey": "S1",
                                    "sessionName": "Race 1",
                                    "sessionType": "Race",
                                    "year": 2024,
                                    "country": "UK",
                                    "dateStart": "%s",
                                    "dateEnd": "%s"
                                  }
                                ]
                                """.formatted(now, now))
                        )));

        // when
        final Either<RaceRetrievalError, List<Event>> result = retriever(1).getEvents(givenYear, givenCountry, givenSessionType);

        // then
        assertThat(result).containsOnRight(List.of(
                new Event("S1", "Race 1", "Race", 2024, "UK", OffsetDateTime.parse(now), OffsetDateTime.parse(now))
        ));
        verify(getRequestedFor(urlPathEqualTo("/sessions")).withQueryParam(testedParamName, equalTo(testedParamValue)));
    }


    @Test
    void shouldMap422ToQueryTooBroad() {
        f1ApiMock.stubFor(get(urlPathEqualTo("/sessions"))
                .willReturn(aResponse().withStatus(422)));

        final Either<RaceRetrievalError, List<Event>> result = retriever(1).getEvents(null, null, "R");
        assertThat(result).containsOnLeft(RaceRetrievalError.QUERY_TOO_BROAD);
    }

    @Test
    void shouldMap429ToRateLimited() {
        f1ApiMock.stubFor(get(urlPathEqualTo("/sessions"))
                .willReturn(aResponse().withStatus(429)));

        final Either<RaceRetrievalError, List<Event>> result = retriever(1).getEvents(null, null, "R");
        assertThat(result).containsOnLeft(RaceRetrievalError.RATE_LIMITED);
    }

    @Test
    void shouldMap5xxToInternalFailure() {
        f1ApiMock.stubFor(get(urlPathEqualTo("/sessions"))
                .willReturn(aResponse().withStatus(500)));

        final Either<RaceRetrievalError, List<Event>> result = retriever(1).getEvents(null, null, "R");
        assertThat(result).containsOnLeft(RaceRetrievalError.INTERNAL_FAILURE);
    }

    @Test
    void shouldGetDriversById() {
        f1ApiMock.stubFor(get(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo("S1"))
                .withQueryParam("driver_number", equalTo("44"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [ { "driverNumber": 44, "fullName": "Lewis", "teamName": "Team" } ]
                                """)));

        final var res = retriever(1).getDrivers("S1", 44);
        assertThat(res).containsOnRight(List.of(new Driver(44, "Lewis", "Team")));
        verify(getRequestedFor(urlPathEqualTo("/drivers")).withQueryParam("driver_number", equalTo("44")));
    }


    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"}, mode = EnumSource.Mode.INCLUDE)
    void shouldRetryOn5xxAndSucceed(HttpStatus httpStatus) {
        final int statusCode = httpStatus.value();
        final String scenario = "retry-" + statusCode;
        f1ApiMock.stubFor(get(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo("S1"))
                .inScenario(scenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(statusCode))
                .willSetStateTo("second"));

        f1ApiMock.stubFor(get(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo("S1"))
                .inScenario(scenario)
                .whenScenarioStateIs("second")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [ { "driverNumber": 1, "fullName": "Max", "teamName": "RB" } ]
                                """)));

        final var result = retriever(2).getDrivers("S1");
        assertThat(result).containsOnRight(List.of(new Driver(1, "Max", "RB")));
        verify(2, getRequestedFor(urlPathEqualTo("/drivers")).withQueryParam("session_key", equalTo("S1")));
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"}, mode = EnumSource.Mode.INCLUDE)
    void shouldReturnInternalFailureAfterRetryExhausted(HttpStatus httpStatus) {
        final int statusCode = httpStatus.value();
        f1ApiMock.stubFor(get(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo("S1"))
                .willReturn(aResponse().withStatus(statusCode)));

        final var result = retriever(3).getDrivers("S1");
        assertThat(result).containsOnLeft(RaceRetrievalError.INTERNAL_FAILURE);
        verify(3, getRequestedFor(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo("S1")));
    }

    @Test
    void shouldGetDriversWithoutDriverId() {
        f1ApiMock.stubFor(get(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo("S1"))
                .withQueryParam("driver_number", absent())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  { "driverNumber": 11, "fullName": "Sergio", "teamName": "RB" },
                                  { "driverNumber": 16, "fullName": "Charles", "teamName": "Ferrari" }
                                ]
                                """)));

        final var res = retriever(1).getDrivers("S1");
        assertThat(res).containsOnRight(List.of(
                new Driver(11, "Sergio", "RB"),
                new Driver(16, "Charles", "Ferrari")
        ));
        verify(getRequestedFor(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo("S1"))
                .withQueryParam("driver_number", absent()));
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"}, mode = EnumSource.Mode.INCLUDE)
    void shouldRetrySessionsOn5xxAndSucceed(HttpStatus httpStatus) {
        final int statusCode = httpStatus.value();
        final String scenario = "retry-sessions-" + statusCode;
        final String now = "2024-01-01T00:00:00Z";

        f1ApiMock.stubFor(get(urlPathEqualTo("/sessions"))
                .withQueryParam("session_type", equalTo("R"))
                .inScenario(scenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(statusCode))
                .willSetStateTo("second"));

        f1ApiMock.stubFor(get(urlPathEqualTo("/sessions"))
                .withQueryParam("session_type", equalTo("R"))
                .inScenario(scenario)
                .whenScenarioStateIs("second")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(("""
                                [
                                  {
                                    "sessionKey": "S1",
                                    "sessionName": "Race 1",
                                    "sessionType": "R",
                                    "year": 2024,
                                    "country": "UK",
                                    "dateStart": "%s",
                                    "dateEnd": "%s"
                                  }
                                ]
                                """.formatted(now, now)))));

        final var result = retriever(2).getEvents(null, null, "R");
        assertThat(result).containsOnRight(List.of(
                new Event("S1", "Race 1", "R", 2024, "UK", OffsetDateTime.parse(now), OffsetDateTime.parse(now))
        ));
        verify(2, getRequestedFor(urlPathEqualTo("/sessions"))
                .withQueryParam("session_type", equalTo("R")));
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"}, mode = EnumSource.Mode.INCLUDE)
    void shouldReturnInternalFailureAfterRetryExhaustedSessions(HttpStatus httpStatus) {
        final int statusCode = httpStatus.value();
        f1ApiMock.stubFor(get(urlPathEqualTo("/sessions"))
                .withQueryParam("session_type", equalTo("R"))
                .willReturn(aResponse().withStatus(statusCode)));

        final var result = retriever(3).getEvents(null, null, "R");
        assertThat(result).containsOnLeft(RaceRetrievalError.INTERNAL_FAILURE);
        verify(3, getRequestedFor(urlPathEqualTo("/sessions"))
                .withQueryParam("session_type", equalTo("R")));
    }
}

