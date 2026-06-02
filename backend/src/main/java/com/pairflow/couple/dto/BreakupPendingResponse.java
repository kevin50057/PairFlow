package com.pairflow.couple.dto;

import com.pairflow.couple.DataHandling;
import com.pairflow.couple.PendingBreakup;

import java.time.Instant;

public record BreakupPendingResponse(
        String id,
        String coupleId,
        String initiatorId,
        DataHandling dataHandling,
        Instant expiresAt,
        Instant createdAt
) {
    public static BreakupPendingResponse from(PendingBreakup p) {
        return new BreakupPendingResponse(
                p.getId(), p.getCoupleId(), p.getInitiatorId(),
                p.getDataHandling(), p.getExpiresAt(), p.getCreatedAt());
    }
}
