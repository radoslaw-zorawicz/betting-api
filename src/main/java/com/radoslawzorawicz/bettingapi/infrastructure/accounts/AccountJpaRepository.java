package com.radoslawzorawicz.bettingapi.infrastructure.accounts;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountJpaEntity> findByUserId(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<AccountJpaEntity> findAllByUserIdIn(Collection<Integer> userIds);
}
