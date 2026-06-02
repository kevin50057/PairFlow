package com.pairflow.dateplan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DateCandidateRepository extends JpaRepository<DateCandidate, String> {

    List<DateCandidate> findByPlanIdOrderByCreatedAtAsc(String planId);

    void deleteByPlanId(String planId);
}
