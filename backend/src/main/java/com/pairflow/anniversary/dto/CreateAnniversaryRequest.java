package com.pairflow.anniversary.dto;

import com.pairflow.anniversary.RepeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateAnniversaryRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull LocalDate date,
        RepeatType repeatType,
        List<Integer> reminderDaysBefore,
        @Size(max = 1000) String description
) {
}
