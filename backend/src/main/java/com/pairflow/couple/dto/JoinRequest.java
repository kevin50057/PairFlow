package com.pairflow.couple.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinRequest(@NotBlank String code) {
}
