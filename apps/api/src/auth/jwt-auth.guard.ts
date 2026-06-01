import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import type { Request } from 'express';
import { ApiException } from '../common/api-error';
import { UserStatus } from '../common/enums';
import { PrismaService } from '../prisma/prisma.service';
import type { JwtPayload } from './auth-types';

/** Verifies a Bearer JWT and attaches the live user as req.user. */
@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(
    private readonly jwt: JwtService,
    private readonly prisma: PrismaService,
  ) {}

  async canActivate(ctx: ExecutionContext): Promise<boolean> {
    const req = ctx.switchToHttp().getRequest<Request>();
    const token = this.extractToken(req.headers.authorization);
    if (!token) throw ApiException.unauthorized('請先登入');

    let payload: JwtPayload;
    try {
      payload = await this.jwt.verifyAsync<JwtPayload>(token);
    } catch {
      throw ApiException.unauthorized('登入已過期，請重新登入');
    }

    const user = await this.prisma.user.findUnique({
      where: { id: payload.sub },
      select: { id: true, email: true, nickname: true, role: true, status: true },
    });
    if (!user || user.status !== UserStatus.ACTIVE) {
      throw ApiException.unauthorized('帳號無法使用，請重新登入');
    }

    (req as Request & { user: unknown }).user = {
      id: user.id,
      email: user.email,
      nickname: user.nickname,
      role: user.role,
    };
    return true;
  }

  private extractToken(header?: string): string | null {
    if (!header) return null;
    const [type, value] = header.split(' ');
    return type === 'Bearer' && value ? value : null;
  }
}
