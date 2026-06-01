package com.pairflow.user.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequest(
        @Size(min = 1, max = 50) String displayName,
        String avatarUrl,
        LocalDate birthday,
        String timezone
) {
}
