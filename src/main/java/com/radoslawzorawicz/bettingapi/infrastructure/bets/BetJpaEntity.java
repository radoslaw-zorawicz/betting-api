package com.radoslawzorawicz.bettingapi.infrastructure.bets;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.radoslawzorawicz.bettingapi.domain.bets.Bet.*;

@Entity
@Table(name = "bets")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @NotNull
    @Column(name = "driver_id", nullable = false)
    private Integer driverId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BetStatus status;

    @NotNull
    @Column(name = "odds", nullable = false)
    private Integer odds;
}

