package com.pairflow.couple;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleInviteRepository extends JpaRepository<CoupleInvite, String> {

    Optional<CoupleInvite> findByCode(String code);

    boolean existsByCode(String code);

    Optional<CoupleInvite> findFirstByInviterUserIdAndStatusOrderByCreatedAtDesc(String inviterUserId, InviteStatus status);
}
