package com.pairflow.anniversary.dto;

import com.pairflow.anniversary.RepeatType;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateAnniversaryRequest(
        @Size(max = 200) String title,
        LocalDate date,
        RepeatType repeatType,
        List<Integer> reminderDaysBefore,
        @Size(max = 1000) String description
) {
}
