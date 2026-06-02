package com.pairflow.dateplan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatePlanRepository extends JpaRepository<DatePlan, String> {

    List<DatePlan> findByCoupleIdOrderByCreatedAtDesc(String coupleId);
}
