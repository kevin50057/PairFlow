package com.pairflow.repair;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairSessionRepository extends JpaRepository<RepairSession, String> {

    List<RepairSession> findByCoupleIdOrderByCreatedAtDesc(String coupleId);
}
