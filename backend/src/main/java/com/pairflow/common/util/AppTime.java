package com.pairflow.common.util;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Single source of truth for "what day is it" across the app. Day-based features
 * (days-together, countdowns, "today" todos, "去年今天" memories) all resolve the
 * current date in one zone so the numbers are consistent and match the spec.
 */
public final class AppTime {

    public static final ZoneId ZONE = ZoneId.of("Asia/Taipei");

    private AppTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}
