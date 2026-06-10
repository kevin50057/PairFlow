package com.pairflow.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, String>, JpaSpecificationExecutor<Todo> {

    /** Visible, not-done todos due within a window — used by the home dashboard. */
    List<Todo> findByCoupleIdAndStatusInAndDueDateBetween(
            String coupleId, List<TodoStatus> statuses, Instant from, Instant to);

    long countByCoupleIdAndStatusInAndDueDateBetween(
            String coupleId, List<TodoStatus> statuses, Instant from, Instant to);

    /** Used by the scheduler to find globally due todos across all couples. */
    List<Todo> findByStatusAndDueDateBetween(TodoStatus status, Instant from, Instant to);

    /** Auto-complete: scheduled todos whose due time has already passed. */
    List<Todo> findByStatusAndAutoCompleteTrueAndDueDateLessThanEqual(TodoStatus status, Instant time);
}
