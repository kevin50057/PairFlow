package com.pairflow.event.dto;

import com.pairflow.event.EventType;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateEventRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        EventType eventType,
        Instant startTime,
        Instant endTime,
        String locationName,
        String locationAddress,
        Instant reminderTime,
        Double budget,
        String transport,
        String dressCode,
        String reservationInfo,
        String relatedTodoId,
        String relatedAlbumId
) {
}
