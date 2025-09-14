package com.radoslawzorawicz.bettingapi.infrastructure.accounts;

import com.radoslawzorawicz.bettingapi.domain.accounts.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JpaAdaptersConfig {

    @Bean
    AccountRepository accountRepository(AccountJpaRepository accountJpaRepository) {
        return new AccountRepositoryJpaAdapter(accountJpaRepository);
    }
}
