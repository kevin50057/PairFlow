import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import type { AuthUser } from './auth-types';

/** Returns the authenticated user attached by JwtAuthGuard. */
export const CurrentUser = createParamDecorator((_data: unknown, ctx: ExecutionContext): AuthUser => {
  return ctx.switchToHttp().getRequest().user as AuthUser;
});
