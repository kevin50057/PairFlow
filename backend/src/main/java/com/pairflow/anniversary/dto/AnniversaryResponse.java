package com.pairflow.anniversary.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AnniversaryResponse(
        String id,
        String coupleId,
        String title,
        LocalDate date,
        String repeatType,
        List<Integer> reminderDaysBefore,
        String description,
        LocalDate nextOccurrence,
        long daysLeft,
        String createdBy,
        Instant createdAt
) {
}
