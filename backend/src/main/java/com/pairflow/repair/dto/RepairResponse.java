package com.pairflow.repair.dto;

import java.time.Instant;

public record RepairResponse(
        String id,
        String coupleId,
        String initiatorId,
        String state,
        /** Raw feelings — null unless the viewer is the initiator. */
        String feelings,
        String keyPoints,
        String softenedMessage,
        String status,
        boolean flagged,
        /** Safety help text when the message was flagged (initiator only). */
        String notice,
        String responderId,
        String responseType,
        String responseNote,
        Instant respondedAt,
        Instant createdAt
) {
}
