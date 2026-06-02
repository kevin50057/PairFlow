package com.pairflow.finance.dto;

import java.util.Map;

public record ExpenseSummary(
        double total,
        long count,
        Map<String, Double> byCategory,
        Map<String, Double> byPayer
) {
}
