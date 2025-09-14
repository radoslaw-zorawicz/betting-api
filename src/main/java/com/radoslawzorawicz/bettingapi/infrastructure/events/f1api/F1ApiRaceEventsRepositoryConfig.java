package com.radoslawzorawicz.bettingapi.infrastructure.events.f1api;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@EnableConfigurationProperties(OpenF1Properties.class)
@Configuration
public class F1ApiRaceEventsRepositoryConfig {

    @Bean
    F1ApiEventsRetriever apiRaceEventsReadRepository(RestClient.Builder restClientBuilder, OpenF1Properties props) {
        return new F1ApiEventsRetriever(restClientBuilder, props);
    }
}
