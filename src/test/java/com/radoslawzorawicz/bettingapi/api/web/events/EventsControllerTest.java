package com.radoslawzorawicz.bettingapi.api.web.events;

import com.radoslawzorawicz.bettingapi.application.events.EventsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.events.model.*;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EventsControllerTest {

    @Mock
    private EventsApplicationService eventsService;
    @InjectMocks
    private EventsController controller;

    @BeforeEach
    void setup() {
        standaloneSetup(controller);
    }

    @Test
    void shouldReturnEvents() {
        // given
        final var now = OffsetDateTime.now();
        given(eventsService.getEvents(null, null, null)).willReturn(Either.right(List.of(
                new Event("E1", "Race 1", "R", 2024, "UK", now, now.plusHours(2))
        )));

        // expect
        given()
        .when()
            .get("/events")
        .then()
            .statusCode(200)
            .body("$.size()", equalTo(1))
            .body("[0].eventId", equalTo("E1"));
    }

    @Test
    void shouldReturnEventsWithAllParams() {
        // given
        final var now = OffsetDateTime.now();
        given(eventsService.getEvents(2024, 5, "R")).willReturn(Either.right(List.of(
                new Event("E2", "Race 2", "R", 2024, "UK", now, now.plusHours(2))
        )));

        // expect
        given()
            .queryParam("year", 2024)
            .queryParam("meeting_key", 5)
            .queryParam("session_type", "R")
        .when()
            .get("/events")
        .then()
            .statusCode(200)
            .body("$.size()", equalTo(1))
            .body("[0].eventId", equalTo("E2"));

        then(eventsService).should().getEvents(2024, 5, "R");
    }

    @ParameterizedTest
    @MethodSource("eventsErrorCases")
    void shouldMapGetEventsErrors(String sessionType, RaceRetrievalError error, int expectedStatus) {
        given(eventsService.getEvents(any(), any(), anyString())).willReturn(Either.left(error));

        given()
            .queryParam("session_type", sessionType)
        .when()
            .get("/events")
        .then()
            .statusCode(expectedStatus);
    }

    private static Stream<Arguments> eventsErrorCases() {
        return Stream.of(
                Arguments.arguments(" ", RaceRetrievalError.QUERY_TOO_BROAD, 422),
                Arguments.arguments("R", RaceRetrievalError.RATE_LIMITED, 429),
                Arguments.arguments("R", RaceRetrievalError.INTERNAL_FAILURE, 500)
        );
    }

    @Test
    void shouldReturnDriversMarket() {
        given(eventsService.getDriversMarket("S1")).willReturn(Either.right(List.of(
                new DriverMarket(new Driver(44, "Driver", "Team"), 3)
        )));

        given()
        .when()
            .get("/events/{session_id}/drivers_market", "S1")
        .then()
            .statusCode(200)
            .body("$.size()", equalTo(1))
            .body("[0].driver.driverNumber", equalTo(44))
            .body("[0].driver.fullName", equalTo("Driver"))
            .body("[0].driver.teamName", equalTo("Team"))
            .body("[0].odds", equalTo(3));
    }

    @Test
    void shouldMapDriversMarketErrors() {
        given(eventsService.getDriversMarket("S1")).willReturn(Either.left(RaceRetrievalError.INTERNAL_FAILURE));

        given()
        .when()
            .get("/events/{session_id}/drivers_market", "S1")
        .then()
            .statusCode(500);
    }

    @Test
    void shouldSettleEvent() {
        given(eventsService.finishEvent("E1", 44)).willReturn(Either.right(null));

        given()
            .contentType("application/json")
            .body("""
                  {
                    "winningDriverId": 44
                  }
                  """)
        .when()
            .post("/events/{event_id}/settlement", "E1")
        .then()
            .statusCode(200);
    }

    private static Stream<Arguments> settlementErrorCases() {
        return Stream.of(
                Arguments.arguments(SettlementError.INVALID_REQUEST, 400),
                Arguments.arguments(SettlementError.EVENT_ALREADY_FINISHED, 409),
                Arguments.arguments(SettlementError.INTERNAL_ERROR, 500)
        );
    }

    @ParameterizedTest
    @MethodSource("settlementErrorCases")
    void shouldMapSettlementErrors(SettlementError error, int expectedStatus) {
        given(eventsService.finishEvent(eq("E1"), anyInt())).willReturn(Either.left(error));

        final String payload = """
                {
                  "winningDriverId": 44
                }
                """;
        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("/events/{event_id}/settlement", "E1")
        .then()
            .statusCode(expectedStatus);
    }

    @Test
    void shouldRejectInvalidSettlementPayload() {
        // missing winningDriverId
        given()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("/events/{event_id}/settlement", "E1")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturn400WhenSettlementBodyMissing() {
        given()
        .when()
            .post("/events/{event_id}/settlement", "E1")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldRejectZeroWinningDriverId() {
        given(eventsService.finishEvent("E1", 0)).willReturn(Either.left(SettlementError.INVALID_REQUEST));
        final String payload = """
                {
                  "winningDriverId": 0
                }
                """;
        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("/events/{event_id}/settlement", "E1")
        .then()
            .statusCode(400);
    }
}
