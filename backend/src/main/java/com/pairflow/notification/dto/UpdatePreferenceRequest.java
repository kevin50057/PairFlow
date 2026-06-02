package com.pairflow.notification.dto;

import com.pairflow.notification.NotificationType;

import java.util.List;

public record UpdatePreferenceRequest(List<NotificationType> disabledTypes) {
}
