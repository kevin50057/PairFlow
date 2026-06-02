package com.pairflow.repair.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FollowUpRequest(@NotEmpty List<String> tasks) {
}
