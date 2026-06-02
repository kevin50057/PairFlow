package com.pairflow.repair.dto;

import com.pairflow.repair.RepairState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRepairRequest(
        @NotNull RepairState state,
        @NotBlank @Size(max = 2000) String feelings,
        @Size(max = 1000) String keyPoints
) {
}
