package com.pairflow.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddChecklistItemRequest(@NotBlank @Size(max = 300) String title) {
}
