package com.pairflow.common.error;

import lombok.Getter;

/** Domain exception carrying a stable {@link ErrorCode}. Thrown anywhere; turned
 *  into the unified error body by {@link GlobalExceptionHandler}. */
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode code;

    public ApiException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public static ApiException notFound(String message)     { return new ApiException(ErrorCode.NOT_FOUND, message); }
    public static ApiException forbidden(String message)    { return new ApiException(ErrorCode.FORBIDDEN, message); }
    public static ApiException conflict(String message)     { return new ApiException(ErrorCode.CONFLICT, message); }
    public static ApiException unauthorized(String message) { return new ApiException(ErrorCode.UNAUTHORIZED, message); }
    public static ApiException badRequest(String message)   { return new ApiException(ErrorCode.VALIDATION_ERROR, message); }
}
