package com.radoslawzorawicz.bettingapi.infrastructure.bets;

import com.radoslawzorawicz.bettingapi.application.bets.BetsApplicationService;
import com.radoslawzorawicz.bettingapi.application.bets.BetsPlacementService;
import com.radoslawzorawicz.bettingapi.application.events.EventsApplicationService;
import com.radoslawzorawicz.bettingapi.domain.accounts.AccountRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.BetRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BetsServiceConfig {

    @Bean
    BetsApplicationService betsService(AccountRepository accountRepository,
                                      BetRepository betRepository,
                                      EventsApplicationService eventsApplicationService) {
        return new BetsPlacementService(accountRepository, betRepository, eventsApplicationService);
    }

    @Bean
    BetMapper betsMapper() {
        return new BetMapper();
    }

    @Bean
    BetRepository betRepository(BetJpaRepository betJpaRepository, BetMapper betMapper) {
        return new BetRepositoryJpaAdapter(betJpaRepository, betMapper);
    }
}
