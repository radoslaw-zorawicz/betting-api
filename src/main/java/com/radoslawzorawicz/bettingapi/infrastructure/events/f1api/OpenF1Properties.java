package com.radoslawzorawicz.bettingapi.infrastructure.events.f1api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openf1")
record OpenF1Properties(
        String baseUrl,
        Rate rate,
        Retry retry
) {
    record Rate(int limitForPeriod, int limitRefreshSeconds) {
    }

    record Retry(int maxAttempts, long baseDelayMs, double multiplier, double jitterFactor, long maxDelayMs) {
    }
}

