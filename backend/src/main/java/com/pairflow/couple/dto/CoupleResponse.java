package com.pairflow.couple.dto;

import com.pairflow.user.dto.UserResponse;

import java.time.Instant;
import java.time.LocalDate;

public record CoupleResponse(
        String id,
        UserResponse partner,
        LocalDate relationshipStartDate,
        Long daysTogether,
        String status,
        Instant createdAt
) {
}
