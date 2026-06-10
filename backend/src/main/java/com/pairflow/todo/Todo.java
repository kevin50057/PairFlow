package com.pairflow.todo;

import com.pairflow.common.entity.BaseEntity;
import com.pairflow.common.enums.Priority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "todos", indexes = {
        @Index(name = "idx_todo_couple", columnList = "coupleId"),
        @Index(name = "idx_todo_due", columnList = "dueDate")
})
public class Todo extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TodoType type = TodoType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TodoStatus status = TodoStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Priority priority = Priority.MEDIUM;

    /** Concrete assignee when a single person owns it; null when both/unassigned. */
    private String assigneeUserId;

    @Column(nullable = false)
    private boolean assignedToBoth = false;

    private Instant dueDate;
    private Instant reminderTime;

    /** When true and {@link #dueDate} passes, the scheduler auto-marks this todo DONE (預計綁定行事曆). */
    @Column(nullable = false)
    private boolean autoComplete = false;

    /** Simple recurrence hint, e.g. "WEEKLY:MON" or "MONTHLY:1" (spec 7.3 #4). */
    private String repeatRule;

    /** Surprise task: hidden from the partner until {@link #secretUnlockAt} (spec 7.3 #6). */
    @Column(nullable = false)
    private boolean secret = false;

    private Instant secretUnlockAt;

    private String relatedEventId;
    private String relatedAnniversaryId;

    /** Shared-goal progress (spec 7.3 #5), e.g. target 50000 / current 18000 unit "元". */
    private Double goalTarget;
    private Double goalCurrent;
    private String goalUnit;

    @Column(nullable = false)
    private String createdBy;

    private Instant completedAt;
}
