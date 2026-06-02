package com.pairflow.auth.dto;

import com.pairflow.user.dto.UserResponse;

public record AuthResponse(String token, String refreshToken, UserResponse user) {
}
