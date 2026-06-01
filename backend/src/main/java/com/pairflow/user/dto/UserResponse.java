package com.pairflow.user.dto;

import com.pairflow.user.User;

import java.time.Instant;
import java.time.LocalDate;

public record UserResponse(
        String id,
        String email,
        String displayName,
        String avatarUrl,
        LocalDate birthday,
        String timezone,
        Instant createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(), u.getEmail(), u.getDisplayName(),
                u.getAvatarUrl(), u.getBirthday(), u.getTimezone(), u.getCreatedAt());
    }
}
