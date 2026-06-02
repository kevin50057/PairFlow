package com.pairflow.notification.dto;

import com.pairflow.notification.Notification;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String type,
        String title,
        String body,
        boolean isRead,
        String relatedType,
        String relatedId,
        Instant createdAt,
        Instant readAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType().name(), n.getTitle(), n.getBody(),
                n.isRead(), n.getRelatedType(), n.getRelatedId(), n.getCreatedAt(), n.getReadAt());
    }
}
