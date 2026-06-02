package com.pairflow.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByActorIdOrderByCreatedAtDesc(String actorId, Pageable pageable);

    List<AuditLog> findByCoupleIdOrderByCreatedAtDesc(String coupleId, Pageable pageable);
}
