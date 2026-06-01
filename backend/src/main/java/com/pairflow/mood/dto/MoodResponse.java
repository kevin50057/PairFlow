package com.pairflow.mood.dto;

import java.time.Instant;
import java.util.List;

public record MoodResponse(
        String id,
        String userId,
        String mood,
        String emoji,
        String note,
        boolean needResponse,
        List<ReactionResponse> reactions,
        Instant createdAt
) {
}
