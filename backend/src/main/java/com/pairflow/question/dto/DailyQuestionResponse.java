package com.pairflow.question.dto;

import java.time.LocalDate;

public record DailyQuestionResponse(
        String id,
        LocalDate date,
        String questionText,
        String category,
        String sensitivity,
        String myAnswer,
        /** Revealed only once both partners have answered (spec 7.9). */
        String partnerAnswer,
        boolean myAnswered,
        boolean partnerAnswered,
        boolean bothAnswered,
        boolean isFavorite
) {
}
