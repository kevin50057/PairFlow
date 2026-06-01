package com.pairflow.mood.dto;

import com.pairflow.mood.MoodType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMoodRequest(
        @NotNull MoodType mood,
        String emoji,
        @Size(max = 500) String note,
        Boolean needResponse
) {
}
