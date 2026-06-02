package com.pairflow.finance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, String> {

    List<Expense> findByCoupleIdOrderBySpentAtDesc(String coupleId);

    List<Expense> findByCoupleIdAndSpentAtBetweenOrderBySpentAtDesc(String coupleId, Instant from, Instant to);
}
