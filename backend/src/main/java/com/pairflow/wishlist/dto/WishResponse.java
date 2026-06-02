package com.pairflow.wishlist.dto;

import com.pairflow.wishlist.Wish;

import java.time.Instant;

public record WishResponse(
        String id,
        String coupleId,
        String title,
        String description,
        String category,
        String priority,
        Double estimatedCost,
        String location,
        String link,
        String addedBy,
        String status,
        String convertedTodoId,
        Instant completedAt,
        Instant createdAt
) {
    public static WishResponse from(Wish w) {
        return new WishResponse(w.getId(), w.getCoupleId(), w.getTitle(), w.getDescription(),
                w.getCategory().name(), w.getPriority().name(), w.getEstimatedCost(), w.getLocation(),
                w.getLink(), w.getAddedBy(), w.getStatus().name(), w.getConvertedTodoId(),
                w.getCompletedAt(), w.getCreatedAt());
    }
}
