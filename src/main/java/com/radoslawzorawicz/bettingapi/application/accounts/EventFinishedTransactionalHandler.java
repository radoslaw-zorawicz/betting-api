package com.radoslawzorawicz.bettingapi.application.accounts;

import com.radoslawzorawicz.bettingapi.domain.events.model.EventFinished;
import com.radoslawzorawicz.bettingapi.domain.events.model.EventFinishedHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventFinishedTransactionalHandler implements EventFinishedHandler {

    private final BetsSettlementService betsSettlementService;


    @Override
    public void handle(EventFinished event) {
        betsSettlementService.settle(event.eventId(), event.winningDriverId());
    }
}
