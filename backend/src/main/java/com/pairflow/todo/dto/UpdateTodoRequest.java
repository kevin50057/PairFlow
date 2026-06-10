package com.pairflow.todo.dto;

import com.pairflow.common.enums.Priority;
import com.pairflow.todo.AssigneeOption;
import com.pairflow.todo.TodoStatus;
import com.pairflow.todo.TodoType;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** Partial update — null fields are left unchanged. */
public record UpdateTodoRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        TodoType type,
        TodoStatus status,
        Priority priority,
        AssigneeOption assignee,
        Instant dueDate,
        Instant reminderTime,
        Boolean autoComplete,
        String repeatRule,
        Boolean isSecret,
        Instant secretUnlockAt,
        String relatedEventId,
        String relatedAnniversaryId,
        Double goalTarget,
        Double goalCurrent,
        String goalUnit
) {
}
