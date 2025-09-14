package com.radoslawzorawicz.bettingapi.domain.events.service;

import com.radoslawzorawicz.bettingapi.domain.events.model.Odds;

public interface OddsPolicy {
    Odds nextOdds();
}
