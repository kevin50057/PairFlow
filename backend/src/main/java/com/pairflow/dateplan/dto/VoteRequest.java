package com.pairflow.dateplan.dto;

import com.pairflow.dateplan.VoteType;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(@NotNull VoteType vote) {
}
