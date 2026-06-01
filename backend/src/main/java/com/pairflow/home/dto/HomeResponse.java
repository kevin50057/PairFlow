package com.pairflow.home.dto;

import com.pairflow.event.dto.EventResponse;
import com.pairflow.mood.dto.MoodResponse;
import com.pairflow.todo.dto.TodoResponse;

import java.util.List;

/** Single aggregated payload for the daily home dashboard (spec 22). */
public record HomeResponse(
        HomeCouple couple,
        MoodResponse partnerMood,
        List<TodoResponse> todayTodos,
        HomeAnniversary nextAnniversary,
        List<EventResponse> todayEvents,
        HomeMemory memory
) {
}
