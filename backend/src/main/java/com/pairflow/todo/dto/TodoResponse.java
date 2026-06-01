package com.pairflow.todo.dto;

import java.time.Instant;
import java.util.List;

public record TodoResponse(
        String id,
        String coupleId,
        String title,
        String description,
        String type,
        String status,
        String priority,
        /** Viewer-relative: "me" | "partner" | "both" | "unassigned". */
        String assignee,
        String assigneeUserId,
        boolean assignedToBoth,
        Instant dueDate,
        Instant reminderTime,
        String repeatRule,
        boolean isSecret,
        Instant secretUnlockAt,
        String relatedEventId,
        String relatedAnniversaryId,
        Double goalTarget,
        Double goalCurrent,
        String goalUnit,
        List<ChecklistItemResponse> checklist,
        List<CommentResponse> comments,
        String createdBy,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {
}
