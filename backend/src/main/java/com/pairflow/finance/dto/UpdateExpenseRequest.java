package com.pairflow.finance.dto;

import com.pairflow.finance.PayerOption;
import com.pairflow.finance.SplitType;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record UpdateExpenseRequest(
        @Positive Double amount,
        String category,
        PayerOption paidBy,
        SplitType splitType,
        Double customPayerRatio,
        String note,
        String relatedEventId,
        Instant spentAt
) {
}
