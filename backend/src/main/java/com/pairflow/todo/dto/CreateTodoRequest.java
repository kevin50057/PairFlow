package com.pairflow.todo.dto;

import com.pairflow.common.enums.Priority;
import com.pairflow.todo.AssigneeOption;
import com.pairflow.todo.TodoType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record CreateTodoRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        TodoType type,
        Priority priority,
        AssigneeOption assignee,
        Instant dueDate,
        Instant reminderTime,
        String repeatRule,
        Boolean isSecret,
        Instant secretUnlockAt,
        String relatedEventId,
        String relatedAnniversaryId,
        Double goalTarget,
        Double goalCurrent,
        String goalUnit,
        List<String> checklist
) {
}
