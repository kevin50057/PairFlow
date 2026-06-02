package com.pairflow.dateplan.dto;

import java.time.Instant;
import java.util.List;

public record DatePlanResponse(
        String id,
        String coupleId,
        String title,
        String dateType,
        String budgetLevel,
        String area,
        Integer durationHours,
        String status,
        String chosenCandidateId,
        String scheduledEventId,
        List<CandidateResponse> candidates,
        String createdBy,
        Instant createdAt
) {
}
