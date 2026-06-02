package com.pairflow.finance.dto;

import com.pairflow.finance.Expense;

import java.time.Instant;

public record ExpenseResponse(
        String id,
        String coupleId,
        double amount,
        String category,
        String paidByUserId,
        String splitType,
        Double customPayerRatio,
        String note,
        String relatedEventId,
        Instant spentAt,
        String createdBy,
        Instant createdAt
) {
    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(e.getId(), e.getCoupleId(), e.getAmount(), e.getCategory(),
                e.getPaidByUserId(), e.getSplitType().name(), e.getCustomPayerRatio(), e.getNote(),
                e.getRelatedEventId(), e.getSpentAt(), e.getCreatedBy(), e.getCreatedAt());
    }
}
