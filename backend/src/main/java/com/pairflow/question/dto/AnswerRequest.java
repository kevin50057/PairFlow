package com.pairflow.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerRequest(@NotBlank @Size(max = 2000) String answer) {
}
