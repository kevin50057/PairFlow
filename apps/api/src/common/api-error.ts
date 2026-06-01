import { HttpException, HttpStatus } from '@nestjs/common';
import type { ValidationError } from 'class-validator';

// Standardized error contract shared by the whole API (see spec §14).
export type ApiErrorCode =
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'NOT_FOUND'
  | 'VALIDATION_ERROR'
  | 'CONFLICT'
  | 'RATE_LIMITED'
  | 'INTERNAL_ERROR';

export const STATUS_BY_CODE: Record<ApiErrorCode, HttpStatus> = {
  UNAUTHORIZED: HttpStatus.UNAUTHORIZED,
  FORBIDDEN: HttpStatus.FORBIDDEN,
  NOT_FOUND: HttpStatus.NOT_FOUND,
  VALIDATION_ERROR: HttpStatus.BAD_REQUEST,
  CONFLICT: HttpStatus.CONFLICT,
  RATE_LIMITED: HttpStatus.TOO_MANY_REQUESTS,
  INTERNAL_ERROR: HttpStatus.INTERNAL_SERVER_ERROR,
};

export interface ApiErrorBody {
  code: ApiErrorCode;
  message: string;
  details: unknown[];
}

export interface FieldError {
  field: string;
  message: string;
}

/** Throwable application error that already carries our standard response body. */
export class ApiException extends HttpException {
  constructor(
    public readonly code: ApiErrorCode,
    message: string,
    public readonly details: unknown[] = [],
  ) {
    super({ code, message, details } satisfies ApiErrorBody, STATUS_BY_CODE[code]);
  }

  static notFound(message = 'Resource not found') {
    return new ApiException('NOT_FOUND', message);
  }
  static conflict(message: string, details: unknown[] = []) {
    return new ApiException('CONFLICT', message, details);
  }
  static validation(message: string, details: unknown[] = []) {
    return new ApiException('VALIDATION_ERROR', message, details);
  }
  static unauthorized(message = 'Authentication required') {
    return new ApiException('UNAUTHORIZED', message);
  }
  static forbidden(message = 'Not allowed') {
    return new ApiException('FORBIDDEN', message);
  }
}

/** Converts class-validator failures into a VALIDATION_ERROR with field details. */
export function buildValidationError(errors: ValidationError[]): ApiException {
  const details = flattenValidationErrors(errors);
  const message = details[0]?.message ?? 'Validation failed';
  return ApiException.validation(message, details);
}

function flattenValidationErrors(errors: ValidationError[], parentPath = ''): FieldError[] {
  const out: FieldError[] = [];
  for (const err of errors) {
    const path = parentPath ? `${parentPath}.${err.property}` : err.property;
    if (err.constraints) {
      for (const message of Object.values(err.constraints)) out.push({ field: path, message });
    }
    if (err.children?.length) out.push(...flattenValidationErrors(err.children, path));
  }
  return out;
}
