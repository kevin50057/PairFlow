package com.pairflow.couple;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingBreakupRepository extends JpaRepository<PendingBreakup, String> {

    Optional<PendingBreakup> findFirstByCoupleIdAndCancelledFalseAndConfirmedFalseOrderByCreatedAtDesc(String coupleId);
}
