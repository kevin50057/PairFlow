package com.pairflow.mood.dto;

import com.pairflow.mood.ReactionType;
import jakarta.validation.constraints.NotNull;

public record AddReactionRequest(@NotNull ReactionType reaction) {
}
