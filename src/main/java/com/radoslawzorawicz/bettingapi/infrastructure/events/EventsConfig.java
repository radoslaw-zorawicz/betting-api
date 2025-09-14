package com.radoslawzorawicz.bettingapi.infrastructure.events;

import com.radoslawzorawicz.bettingapi.application.events.EventsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.events.model.DomainEventPublisher;
import com.radoslawzorawicz.bettingapi.domain.events.model.EventOutcomeRepository;
import com.radoslawzorawicz.bettingapi.domain.events.model.RaceEventsReadRepository;
import com.radoslawzorawicz.bettingapi.domain.events.service.OddsPolicy;
import com.radoslawzorawicz.bettingapi.domain.events.service.RandomOddsPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventsConfig {

    @Bean
    public EventsApplicationService eventsApplicationService(RaceEventsReadRepository raceEventsReadRepository,
                                                            EventOutcomeRepository eventOutcomeRepository,
                                                            OddsPolicy oddsPolicy,
                                                            DomainEventPublisher eventPublisher) {
        return new EventsApplicationService(raceEventsReadRepository, eventOutcomeRepository, oddsPolicy, eventPublisher);
    }

    @Bean
    public EventOutcomeRepository eventOutcomeRepository(EventOutcomeJpaRepository jpa) {
        return new EventOutcomeRepositoryJpaAdapter(jpa);
    }

    @Bean
    public OddsPolicy oddsPolicy() {
        return new RandomOddsPolicy();
    }
}
