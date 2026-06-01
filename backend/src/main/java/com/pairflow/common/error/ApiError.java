package com.pairflow.common.error;

import java.util.List;

/** Unified error body shared by every endpoint: {@code { code, message, details[] }}. */
public record ApiError(String code, String message, List<String> details) {

    public static ApiError of(String code, String message, List<String> details) {
        return new ApiError(code, message, details == null ? List.of() : details);
    }
}
