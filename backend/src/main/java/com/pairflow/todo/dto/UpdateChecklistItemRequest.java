package com.pairflow.todo.dto;

import jakarta.validation.constraints.Size;

public record UpdateChecklistItemRequest(
        @Size(max = 300) String title,
        Boolean isCompleted
) {
}
