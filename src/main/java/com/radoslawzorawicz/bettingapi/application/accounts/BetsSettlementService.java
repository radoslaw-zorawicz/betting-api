package com.radoslawzorawicz.bettingapi.application.accounts;

import com.radoslawzorawicz.bettingapi.domain.accounts.Account;
import com.radoslawzorawicz.bettingapi.domain.accounts.AccountRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.Bet;
import com.radoslawzorawicz.bettingapi.domain.bets.BetRepository;
import com.radoslawzorawicz.bettingapi.domain.bets.SettlementPolicy;
import com.radoslawzorawicz.bettingapi.domain.shared.Money;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Log4j2
public class BetsSettlementService {

    private final BetRepository betRepository;
    private final AccountRepository accountRepository;
    private final SettlementPolicy settlementPolicy;

    @Transactional
    public void settle(String eventId, Integer winningDriverId) {
        final List<Bet> pendingBets = betRepository.findAllByEventIdAndStatus(eventId, Bet.BetStatus.PENDING);
        log.info("Pending bets size: {}", pendingBets.size());
        if (pendingBets.isEmpty()) {
            return;
        }

        final List<Bet> statusUpdatedBets = settlementPolicy.resolve(pendingBets, winningDriverId);
        betRepository.saveAll(statusUpdatedBets);

        final Map<Integer, Money> payoutByUserId = settlementPolicy.creditsByUser(statusUpdatedBets);

        final List<Account> winnerAccounts = accountRepository.findAllByUserIdIn(payoutByUserId.keySet());
        final List<Account> updatedWinnerAccounts = updateWinnerAccountsBalance(winnerAccounts, payoutByUserId);

        accountRepository.saveAll(updatedWinnerAccounts);
    }

    private List<Account> updateWinnerAccountsBalance(List<Account> winnerAccounts, Map<Integer, Money> payoutByUserId) {
        return winnerAccounts.stream()
                .map(account -> {
                    final Money payout = payoutByUserId.get(account.userId());
                    return account.credit(payout);
                })
                .toList();
    }
}
