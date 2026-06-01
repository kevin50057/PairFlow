package com.pairflow.mood.dto;

/** Both partners' latest mood for today (either may be null if not posted yet). */
public record TodayMoodResponse(MoodResponse me, MoodResponse partner) {
}
