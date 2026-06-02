package com.pairflow.repair.dto;

import com.pairflow.repair.ResponseType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RespondRequest(
        @NotNull ResponseType responseType,
        @Size(max = 1000) String note
) {
}
