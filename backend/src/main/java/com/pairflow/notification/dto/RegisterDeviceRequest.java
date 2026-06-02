package com.pairflow.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceRequest(
        @NotBlank String fcmToken,
        String platform
) {}
