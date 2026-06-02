package com.pairflow.dateplan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FinalizeRequest(
        @NotBlank String candidateId,
        @NotNull Instant startTime,
        Instant endTime,
        Boolean createTodos
) {
}
