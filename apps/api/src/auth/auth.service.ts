import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { User } from '@prisma/client';
import * as bcrypt from 'bcryptjs';
import { ApiException } from '../common/api-error';
import { UserRole, UserStatus } from '../common/enums';
import { PrismaService } from '../prisma/prisma.service';
import type { AuthResult, UserProfile } from './auth-types';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';

const SALT_ROUNDS = 10;

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly jwt: JwtService,
  ) {}

  async register(dto: RegisterDto): Promise<AuthResult> {
    const existing = await this.prisma.user.findUnique({ where: { email: dto.email } });
    if (existing) throw ApiException.conflict('此 Email 已被註冊');

    const passwordHash = await bcrypt.hash(dto.password, SALT_ROUNDS);
    const user = await this.prisma.user.create({
      data: {
        email: dto.email,
        passwordHash,
        nickname: dto.nickname,
        role: UserRole.USER,
        status: UserStatus.ACTIVE,
      },
    });
    return this.authResult(user);
  }

  async login(dto: LoginDto): Promise<AuthResult> {
    const user = await this.prisma.user.findUnique({ where: { email: dto.email } });
    if (!user || !(await bcrypt.compare(dto.password, user.passwordHash))) {
      throw ApiException.unauthorized('Email 或密碼錯誤');
    }
    if (user.status !== UserStatus.ACTIVE) throw ApiException.forbidden('帳號已被停用');
    return this.authResult(user);
  }

  async profile(userId: number): Promise<UserProfile> {
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    if (!user) throw ApiException.notFound('使用者不存在');
    return toProfile(user);
  }

  private authResult(user: User): AuthResult {
    const token = this.jwt.sign({ sub: user.id, role: user.role });
    return { token, user: toProfile(user) };
  }
}

export function toProfile(user: User): UserProfile {
  return {
    id: user.id,
    email: user.email,
    nickname: user.nickname,
    avatarUrl: user.avatarUrl,
    role: user.role,
    status: user.status,
    createdAt: user.createdAt.toISOString(),
  };
}
