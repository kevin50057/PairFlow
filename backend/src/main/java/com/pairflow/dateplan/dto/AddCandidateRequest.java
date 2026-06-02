package com.pairflow.dateplan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCandidateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        String location
) {
}
