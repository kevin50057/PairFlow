import { randomUUID } from 'node:crypto';
import { ArgumentsHost, Catch, ExceptionFilter, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import type { Request, Response } from 'express';
import { ApiErrorBody, ApiErrorCode, ApiException } from './api-error';

/**
 * Catches everything thrown by controllers/services and renders it as the
 * standard { code, message, details } body. Attaches an x-request-id to every
 * response and emits a structured log line.
 */
@Catch()
export class AllExceptionsFilter implements ExceptionFilter {
  private readonly logger = new Logger('Http');

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const res = ctx.getResponse<Response>();
    const req = ctx.getRequest<Request>();

    const requestId = (req.headers['x-request-id'] as string) || randomUUID();
    res.setHeader('x-request-id', requestId);

    const { status, body } = this.resolve(exception);

    const log = {
      requestId,
      method: req.method,
      path: req.originalUrl,
      status,
      code: body.code,
      message: body.message,
    };
    if (status >= 500) {
      this.logger.error(JSON.stringify(log), exception instanceof Error ? exception.stack : undefined);
    } else {
      this.logger.warn(JSON.stringify(log));
    }

    res.status(status).json(body);
  }

  private resolve(exception: unknown): { status: number; body: ApiErrorBody } {
    if (exception instanceof ApiException) {
      return { status: exception.getStatus(), body: exception.getResponse() as ApiErrorBody };
    }

    if (exception instanceof Prisma.PrismaClientKnownRequestError) {
      if (exception.code === 'P2002') return this.make(HttpStatus.CONFLICT, 'CONFLICT', 'Resource already exists');
      if (exception.code === 'P2025') return this.make(HttpStatus.NOT_FOUND, 'NOT_FOUND', 'Resource not found');
    }

    // Multer (file upload) errors — e.g. file too large.
    if (exception instanceof Error && exception.name === 'MulterError') {
      const code = (exception as Error & { code?: string }).code;
      const message = code === 'LIMIT_FILE_SIZE' ? '圖片大小不可超過 5MB' : '檔案上傳失敗';
      return this.make(HttpStatus.BAD_REQUEST, 'VALIDATION_ERROR', message);
    }

    if (exception instanceof HttpException) {
      const status = exception.getStatus();
      const resp = exception.getResponse();
      const raw =
        typeof resp === 'string'
          ? resp
          : ((resp as { message?: string | string[] })?.message ?? exception.message);
      const message = Array.isArray(raw) ? raw.join(', ') : String(raw);
      return this.make(status, this.codeForStatus(status), message);
    }

    return this.make(HttpStatus.INTERNAL_SERVER_ERROR, 'INTERNAL_ERROR', 'Something went wrong');
  }

  private make(status: number, code: ApiErrorCode, message: string): { status: number; body: ApiErrorBody } {
    return { status, body: { code, message, details: [] } };
  }

  private codeForStatus(status: number): ApiErrorCode {
    switch (status) {
      case HttpStatus.BAD_REQUEST:
        return 'VALIDATION_ERROR';
      case HttpStatus.UNAUTHORIZED:
        return 'UNAUTHORIZED';
      case HttpStatus.FORBIDDEN:
        return 'FORBIDDEN';
      case HttpStatus.NOT_FOUND:
        return 'NOT_FOUND';
      case HttpStatus.CONFLICT:
        return 'CONFLICT';
      case HttpStatus.TOO_MANY_REQUESTS:
        return 'RATE_LIMITED';
      default:
        return status >= 500 ? 'INTERNAL_ERROR' : 'VALIDATION_ERROR';
    }
  }
}
