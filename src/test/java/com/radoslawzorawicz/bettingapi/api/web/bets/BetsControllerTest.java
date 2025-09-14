package com.radoslawzorawicz.bettingapi.api.web.bets;

import com.radoslawzorawicz.bettingapi.application.bets.BetsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.BetPlacementError;
import com.radoslawzorawicz.bettingapi.domain.bets.PlaceBetCommand;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BetsControllerTest {

    @Mock
    private BetsApplicationService betsService;

    @InjectMocks
    private BetsController controller;

    @BeforeEach
    void setup() {
        standaloneSetup(controller);
    }

    @Test
    void shouldReturnBetsForUser() {
        // given
        final var bet1 = new Bet(10, "E1", 44, 7, Money.of(new BigDecimal("5.00")), Bet.BetStatus.PENDING, 2);
        final var bet2 = new Bet(11, "E2", 16, 7, Money.of(new BigDecimal("3.50")), Bet.BetStatus.WON, 4);
        given(betsService.getBets(7)).willReturn(List.of(bet1, bet2));

        // expect
        given()
            .header("X-USER-ID", 7)
        .when()
            .get("/bets")
        .then()
            .statusCode(200)
            .body("$.size()", equalTo(2))
            .body("[0].id", equalTo(10))
            .body("[0].eventId", equalTo("E1"))
            .body("[0].amount", equalTo(5.00f))
            .body("[0].odds", equalTo(2))
            .body("[0].status", equalTo("PENDING"))
            .body("[1].id", equalTo(11))
            .body("[1].eventId", equalTo("E2"))
            .body("[1].amount", equalTo(3.50f))
            .body("[1].odds", equalTo(4))
            .body("[1].status", equalTo("WON"));
    }

    @Test
    void shouldCreateBetOnSuccess() {
        // given
        given(betsService.placeBet(eq(7), any(PlaceBetCommand.class))).willReturn(Either.right(99));

        // expect
        given()
            .header("X-USER-ID", 7)
            .contentType("application/json")
            .body("""
                  {
                    "eventId": "E1",
                    "driverId": 44,
                    "betAmount": 10.00
                  }
                  """)
        .when()
            .post("/bets")
        .then()
            .statusCode(201)
            .body("betId", equalTo(99));
    }

    private static Stream<Arguments> errorToStatusCases() {
        return Stream.of(
                arguments(BetPlacementError.ACCOUNT_NOT_FOUND, 400),
                arguments(BetPlacementError.DRIVER_MARKET_NOT_FOUND, 400),
                arguments(BetPlacementError.INSUFFICIENT_FUNDS, 402),
                arguments(BetPlacementError.INTERNAL_ERROR, 500)
        );
    }

    @ParameterizedTest
    @MethodSource("errorToStatusCases")
    void shouldMapErrorsToStatuses(BetPlacementError error, int expectedStatus) {
        // given
        given(betsService.placeBet(eq(7), any(PlaceBetCommand.class))).willReturn(Either.left(error));
        final String payload = """
                {
                  "eventId": "E1",
                  "driverId": 44,
                  "betAmount": 10.00
                }
                """;

        // expect
        given()
            .header("X-USER-ID", 7)
            .contentType("application/json")
            .body(payload)
        .when()
            .post("/bets")
        .then()
            .statusCode(expectedStatus);
    }

    @Test
    void shouldRejectInvalidRequest() {
        // missing eventId
        final String invalid = """
                {
                  "eventId": "",
                  "driverId": -1,
                  "betAmount": 0
                }
                """;
        given()
            .header("X-USER-ID", 7)
            .contentType("application/json")
            .body(invalid)
        .when()
            .post("/bets")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturn400WhenUserHeaderMissing() {
        given()
        .when()
            .get("/bets")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturn400OnInvalidJsonType() {
        final String invalidType = """
                {
                  "eventId": "E1",
                  "driverId": 44,
                  "betAmount": "abc"
                }
                """;
        given()
            .header("X-USER-ID", 7)
            .contentType("application/json")
            .body(invalidType)
        .when()
            .post("/bets")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturn400WhenMissingRequiredField() {
        final String missingField = """
                {
                  "eventId": "E1",
                  "betAmount": 10.00
                }
                """;
        given()
            .header("X-USER-ID", 7)
            .contentType("application/json")
            .body(missingField)
        .when()
            .post("/bets")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturn400WhenPostUserHeaderMissing() {
        final String payload = """
                {
                  "eventId": "E1",
                  "driverId": 44,
                  "betAmount": 10.00
                }
                """;
        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("/bets")
        .then()
            .statusCode(400);
    }
}
