package com.pairflow.couple.dto;

import java.time.Instant;

public record CreateInviteResponse(String code, Instant expiresAt) {
}
