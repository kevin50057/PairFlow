package com.pairflow.event.dto;

import com.pairflow.event.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateEventRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        EventType eventType,
        @NotNull Instant startTime,
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
