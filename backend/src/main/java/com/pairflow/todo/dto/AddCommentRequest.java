package com.pairflow.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(@NotBlank @Size(max = 1000) String content) {
}
