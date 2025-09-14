package com.radoslawzorawicz.bettingapi.infrastructure.accounts;

import com.radoslawzorawicz.bettingapi.application.accounts.BetsSettlementService;
import com.radoslawzorawicz.bettingapi.domain.accounts.AccountRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.BetRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.SettlementPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BetsSettlementServiceConfig {

    @Bean
    SettlementPolicy settlementPolicy() {
        return new SettlementPolicy();
    }

    @Bean
    BetsSettlementService betsSettlementService(BetRepository betRepository, AccountRepository accountRepository, SettlementPolicy settlementPolicy) {
        return new BetsSettlementService(betRepository, accountRepository, settlementPolicy);
    }
}
