package com.pairflow.mood.dto;

import com.pairflow.mood.MoodReaction;

import java.time.Instant;

public record ReactionResponse(String id, String userId, String reaction, Instant createdAt) {

    public static ReactionResponse from(MoodReaction r) {
        return new ReactionResponse(r.getId(), r.getUserId(), r.getReaction().name(), r.getCreatedAt());
    }
}
