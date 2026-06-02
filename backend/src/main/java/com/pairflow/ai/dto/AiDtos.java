package com.pairflow.ai.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** Small request/response records for the AI endpoints, grouped for brevity. */
public final class AiDtos {

    private AiDtos() {
    }

    public record TodoBreakdownRequest(@NotBlank String input) {
    }

    public record DateSuggestionRequest(String dateType, String budget, String area, String mood) {
    }

    public record AnniversaryMessageRequest(@NotBlank String occasion, String tone) {
    }

    public record SoftenRequest(@NotBlank String text) {
    }

    public record MemorySummaryRequest(@NotBlank String context) {
    }

    public record ItemsResponse(List<String> items) {
    }

    public record TextResponse(String text) {
    }

    public record SoftenResponse(String original, String softened, boolean flagged, String notice) {
    }
}
