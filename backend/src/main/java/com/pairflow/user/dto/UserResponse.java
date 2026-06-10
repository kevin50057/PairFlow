package com.pairflow.user.dto;

import com.pairflow.user.Gender;
import com.pairflow.user.User;

import java.time.Instant;
import java.time.LocalDate;

public record UserResponse(
        String id,
        String email,
        String displayName,
        String avatarUrl,
        LocalDate birthday,
        Gender gender,
        String bio,
        String timezone,
        Instant createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(), u.getEmail(), u.getDisplayName(),
                u.getAvatarUrl(), u.getBirthday(), u.getGender(), u.getBio(),
                u.getTimezone(), u.getCreatedAt());
    }
}
