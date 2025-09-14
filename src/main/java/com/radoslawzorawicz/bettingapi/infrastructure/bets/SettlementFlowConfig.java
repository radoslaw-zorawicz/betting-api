package com.radoslawzorawicz.bettingapi.infrastructure.bets;

import com.radoslawzorawicz.bettingapi.application.accounts.BetsSettlementService;
import com.radoslawzorawicz.bettingapi.application.accounts.EventFinishedTransactionalHandler;
import com.radoslawzorawicz.bettingapi.domain.events.model.DomainEventPublisher;
import com.radoslawzorawicz.bettingapi.domain.events.model.EventFinishedHandler;
import com.radoslawzorawicz.bettingapi.infrastructure.events.InMemoryDomainEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SettlementFlowConfig {

    @Bean
    EventFinishedHandler eventFinishedHandler(BetsSettlementService betsSettlementService) {
        return new EventFinishedTransactionalHandler(betsSettlementService);
    }

    @Bean
    DomainEventPublisher domainEventPublisher(EventFinishedHandler handler) {
        return new InMemoryDomainEventPublisher(handler);
    }
}
