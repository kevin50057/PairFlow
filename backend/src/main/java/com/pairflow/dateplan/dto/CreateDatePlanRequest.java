package com.pairflow.dateplan.dto;

import com.pairflow.dateplan.BudgetLevel;
import com.pairflow.dateplan.DateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDatePlanRequest(
        @NotBlank @Size(max = 200) String title,
        DateType dateType,
        BudgetLevel budgetLevel,
        String area,
        Integer durationHours
) {
}
