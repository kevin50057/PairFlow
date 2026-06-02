package com.pairflow.dateplan.dto;

import java.time.Instant;
import java.util.List;

public record CandidateResponse(
        String id,
        String planId,
        String title,
        String description,
        String location,
        String addedBy,
        List<VoteView> votes,
        String myVote,
        Instant createdAt
) {
}
