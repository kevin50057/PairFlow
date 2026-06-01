package com.pairflow.event.dto;

import com.pairflow.event.Event;

import java.time.Instant;

public record EventResponse(
        String id,
        String coupleId,
        String title,
        String description,
        String eventType,
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
        String relatedAlbumId,
        String createdBy,
        Instant createdAt
) {
    public static EventResponse from(Event e) {
        return new EventResponse(
                e.getId(), e.getCoupleId(), e.getTitle(), e.getDescription(),
                e.getEventType().name(), e.getStartTime(), e.getEndTime(),
                e.getLocationName(), e.getLocationAddress(), e.getReminderTime(),
                e.getBudget(), e.getTransport(), e.getDressCode(), e.getReservationInfo(),
                e.getRelatedTodoId(), e.getRelatedAlbumId(), e.getCreatedBy(), e.getCreatedAt());
    }
}
