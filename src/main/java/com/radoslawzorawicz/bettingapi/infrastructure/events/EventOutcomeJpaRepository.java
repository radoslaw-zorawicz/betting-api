package com.radoslawzorawicz.bettingapi.infrastructure.events;

import org.springframework.data.jpa.repository.JpaRepository;

interface EventOutcomeJpaRepository extends JpaRepository<EventOutcomeJpaEntity, String> {
}
