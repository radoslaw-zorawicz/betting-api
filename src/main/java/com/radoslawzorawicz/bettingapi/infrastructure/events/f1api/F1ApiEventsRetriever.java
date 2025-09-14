package com.radoslawzorawicz.bettingapi.infrastructure.events.f1api;

import com.radoslawzorawicz.bettingapi.domain.events.model.Driver;
import com.radoslawzorawicz.bettingapi.domain.events.model.Event;
import com.radoslawzorawicz.bettingapi.domain.events.model.RaceEventsReadRepository;
import com.radoslawzorawicz.bettingapi.domain.events.model.RaceRetrievalError;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.*;

@Log4j2
class F1ApiEventsRetriever implements RaceEventsReadRepository {
    private static final Set<HttpStatusCode> RETRYABLE_HTTP_ERRORS = Set.copyOf(
            EnumSet.of(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT)
    );
    private final RestClient restClient;
    private final RateLimiter rateLimiter;
    private final Retry retry;

    F1ApiEventsRetriever(RestClient.Builder restClientBuilder, OpenF1Properties props) {
        this.restClient = restClientBuilder
                .baseUrl(props.baseUrl())
                .build();

        final var rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(props.rate().limitForPeriod())
                .limitRefreshPeriod(Duration.ofSeconds(props.rate().limitRefreshSeconds()))
                .timeoutDuration(Duration.ZERO)
                .drainPermissionsOnResult(executionResult -> executionResult.fold(
                                error -> error instanceof RestClientResponseException responseException && responseException.getStatusCode().isSameCodeAs(TOO_MANY_REQUESTS),
                                ignored -> false
                        )
                )
                .build();

        this.rateLimiter = RateLimiter.of("openf1", rateLimiterConfig);
        final var retryConfig = RetryConfig.custom()
                .maxAttempts(props.retry().maxAttempts())
                .intervalFunction(IntervalFunction.ofExponentialBackoff(props.retry().baseDelayMs()))
                .retryOnException(this::isRetryable)
                .build();

        this.retry = Retry.of("openf1", retryConfig);
    }

    @Override
    public Either<RaceRetrievalError, List<Event>> getEvents(Integer year, Integer meetingKey, String sessionType) {
        final Supplier<List<F1ApiSessionDto>> getEvents = () -> this.restClient
                .get()
                .uri((UriBuilder builder) -> builder
                        .path("/sessions")
                        .queryParamIfPresent("year", ofNullable(year))
                        .queryParamIfPresent("meeting_key", ofNullable(meetingKey))
                        .queryParamIfPresent("session_type", ofNullable(sessionType).filter(StringUtils::isNotBlank))
                        .build()
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        return executeCall(getEvents, F1ApiSessionDto::toEvent);
    }

    @Override
    public Either<RaceRetrievalError, List<Driver>> getDrivers(String sessionKey, Integer driverId) {
        final Supplier<List<F1ApiDriverDto>> getDrivers = () -> this.restClient
                .get()
                .uri((UriBuilder builder) -> builder
                        .path("/drivers")
                        .queryParam("session_key", sessionKey)
                        .queryParam("driver_number", driverId)
                        .build()
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        return executeCall(getDrivers, F1ApiDriverDto::toDriver);
    }

    @Override
    public Either<RaceRetrievalError, List<Driver>> getDrivers(String sessionKey) {
        final Supplier<List<F1ApiDriverDto>> getDrivers = () -> this.restClient
                .get()
                .uri((UriBuilder builder) -> builder
                        .path("/drivers")
                        .queryParam("session_key", sessionKey)
                        .build()
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        return executeCall(getDrivers, F1ApiDriverDto::toDriver);
    }

    private boolean isRetryable(Throwable ex) {
        return switch (ex) {
            case ResourceAccessException ignored -> true;
            case HttpServerErrorException serverError -> RETRYABLE_HTTP_ERRORS.contains(serverError.getStatusCode());
            default -> false;
        };
    }

    private <T, U> Either<RaceRetrievalError, List<U>> executeCall(Supplier<List<T>> getDrivers, Function<T, U> mapper) {
        final Supplier<List<T>> getWithResilience =
                Retry.decorateSupplier(retry, RateLimiter.decorateSupplier(rateLimiter, getDrivers));

        try {
            return getWithResilience.get()
                    .stream()
                    .map(mapper)
                    .collect(collectingAndThen(toList(), Either::right));
        } catch (RestClientResponseException responseException) {
            log.info("Rest client exception", responseException);
            return switch (responseException.getStatusCode()) {
                case UNPROCESSABLE_ENTITY -> Either.left(RaceRetrievalError.QUERY_TOO_BROAD);
                case TOO_MANY_REQUESTS -> Either.left(RaceRetrievalError.RATE_LIMITED);
                default -> Either.left(RaceRetrievalError.INTERNAL_FAILURE);
            };
        } catch (Exception exception) {
            log.error("F1 API GET call failed", exception);
            return Either.left(RaceRetrievalError.INTERNAL_FAILURE);
        }
    }

}
