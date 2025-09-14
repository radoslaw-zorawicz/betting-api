package com.radoslawzorawicz.bettingapi.infrastructure.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "event_outcomes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventOutcomeJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "winning_driver_id", nullable = false)
    private Integer winningDriverId;

    @Column(name = "finished_at", nullable = false)
    private OffsetDateTime finishedAt;

}

