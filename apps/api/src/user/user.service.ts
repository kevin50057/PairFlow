import { Injectable } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import type { UserProfile } from '../auth/auth-types';
import { toProfile } from '../auth/auth.service';
import { buildPage, Page } from '../common/dto/pagination';
import { ReviewStatus } from '../common/enums';
import { PrismaService } from '../prisma/prisma.service';
import { ReviewItem, reviewInclude, toReviewItem } from '../review/review.mapper';
import { UpdateProfileDto } from './dto/update-profile.dto';

// Reviews enriched with their restaurant, for the "my reviews" screen.
const myReviewInclude = {
  ...reviewInclude,
  restaurant: { select: { id: true, name: true, coverImageUrl: true, averageThunderScore: true } },
} satisfies Prisma.ReviewInclude;

type MyReviewRow = Prisma.ReviewGetPayload<{ include: typeof myReviewInclude }>;

export interface MyReviewItem extends ReviewItem {
  restaurant: { id: number; name: string; coverImageUrl: string | null; averageThunderScore: number };
}

@Injectable()
export class UserService {
  constructor(private readonly prisma: PrismaService) {}

  async updateProfile(userId: number, dto: UpdateProfileDto): Promise<UserProfile> {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { nickname: dto.nickname, avatarUrl: dto.avatarUrl },
    });
    return toProfile(user);
  }

  async myReviews(userId: number, page: number, size: number): Promise<Page<MyReviewItem>> {
    const where = { userId, status: { not: ReviewStatus.DELETED } };
    const [rows, total] = await this.prisma.$transaction([
      this.prisma.review.findMany({
        where,
        include: myReviewInclude,
        orderBy: { createdAt: 'desc' },
        skip: page * size,
        take: size,
      }),
      this.prisma.review.count({ where }),
    ]);
    return buildPage(rows.map((r) => this.toMyReview(r)), page, size, total > (page + 1) * size);
  }

  private toMyReview(rv: MyReviewRow): MyReviewItem {
    return {
      ...toReviewItem(rv),
      restaurant: {
        id: rv.restaurant.id,
        name: rv.restaurant.name,
        coverImageUrl: rv.restaurant.coverImageUrl,
        averageThunderScore: rv.restaurant.averageThunderScore,
      },
    };
  }
}
