import { Injectable } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { ApiException } from '../common/api-error';
import { buildPage, Page } from '../common/dto/pagination';
import { RestaurantStatus, ReviewStatus } from '../common/enums';
import { PrismaService } from '../prisma/prisma.service';
import { CreateReviewDto } from './dto/create-review.dto';
import { ReviewItem, reviewInclude, toReviewItem } from './review.mapper';

@Injectable()
export class ReviewService {
  constructor(private readonly prisma: PrismaService) {}

  async listByRestaurant(restaurantId: number, page: number, size: number): Promise<Page<ReviewItem>> {
    await this.ensureRestaurant(restaurantId);
    const where = { restaurantId, status: ReviewStatus.PUBLISHED };
    const [rows, total] = await this.prisma.$transaction([
      this.prisma.review.findMany({
        where,
        include: reviewInclude,
        orderBy: { createdAt: 'desc' },
        skip: page * size,
        take: size,
      }),
      this.prisma.review.count({ where }),
    ]);
    return buildPage(rows.map(toReviewItem), page, size, total > (page + 1) * size);
  }

  async create(restaurantId: number, userId: number, dto: CreateReviewDto): Promise<ReviewItem> {
    await this.ensureRestaurant(restaurantId);

    // One active review per (restaurant, user) for MVP — edit in place instead.
    const existing = await this.prisma.review.findUnique({
      where: { restaurantId_userId: { restaurantId, userId } },
    });
    if (existing) {
      throw ApiException.conflict('You already reviewed this restaurant. Edit your existing review instead.', [
        { reviewId: existing.id },
      ]);
    }

    const tagIds = await this.resolveTagIds(dto.tagCodes);

    // Create review + tags (+ images) and recompute the restaurant aggregate atomically.
    const created = await this.prisma.$transaction(async (tx) => {
      const review = await tx.review.create({
        data: {
          restaurantId,
          userId,
          thunderScore: dto.thunderScore,
          title: dto.title ?? null,
          content: dto.content,
          visitDate: dto.visitDate ? new Date(dto.visitDate) : null,
          status: ReviewStatus.PUBLISHED,
          tags: { create: tagIds.map((reviewTagId) => ({ reviewTagId })) },
          images: dto.imageUrls?.length
            ? { create: dto.imageUrls.map((fileUrl, sortOrder) => ({ fileUrl, sortOrder })) }
            : undefined,
        },
        include: reviewInclude,
      });
      await this.recomputeAggregate(tx, restaurantId);
      return review;
    });

    return toReviewItem(created);
  }

  /** Fetch a single review for its owner (or an admin) — used to prefill edit. */
  async getForEdit(reviewId: number, userId: number, role: string): Promise<ReviewItem> {
    const review = await this.prisma.review.findUnique({ where: { id: reviewId }, include: reviewInclude });
    if (!review || review.status === ReviewStatus.DELETED) throw ApiException.notFound('找不到這則雷評');
    if (review.userId !== userId && role !== 'ADMIN') throw ApiException.forbidden('只能編輯自己的雷評');
    return toReviewItem(review);
  }

  /** Full replace of a review by its owner (PUT semantics). */
  async update(reviewId: number, userId: number, dto: CreateReviewDto): Promise<ReviewItem> {
    const review = await this.prisma.review.findUnique({
      where: { id: reviewId },
      select: { id: true, userId: true, restaurantId: true, status: true },
    });
    if (!review || review.status === ReviewStatus.DELETED) throw ApiException.notFound('找不到這則雷評');
    if (review.userId !== userId) throw ApiException.forbidden('只能編輯自己的雷評');

    const tagIds = await this.resolveTagIds(dto.tagCodes);
    const updated = await this.prisma.$transaction(async (tx) => {
      await tx.reviewTagRel.deleteMany({ where: { reviewId } });
      await tx.reviewImage.deleteMany({ where: { reviewId } });
      const r = await tx.review.update({
        where: { id: reviewId },
        data: {
          thunderScore: dto.thunderScore,
          title: dto.title ?? null,
          content: dto.content,
          visitDate: dto.visitDate ? new Date(dto.visitDate) : null,
          tags: { create: tagIds.map((reviewTagId) => ({ reviewTagId })) },
          images: dto.imageUrls?.length
            ? { create: dto.imageUrls.map((fileUrl, sortOrder) => ({ fileUrl, sortOrder })) }
            : undefined,
        },
        include: reviewInclude,
      });
      await this.recomputeAggregate(tx, review.restaurantId);
      return r;
    });
    return toReviewItem(updated);
  }

  /** Hard delete by owner (frees the one-review-per-restaurant slot). */
  async remove(reviewId: number, userId: number): Promise<{ success: true }> {
    const review = await this.prisma.review.findUnique({
      where: { id: reviewId },
      select: { id: true, userId: true, restaurantId: true },
    });
    if (!review) throw ApiException.notFound('找不到這則雷評');
    if (review.userId !== userId) throw ApiException.forbidden('只能刪除自己的雷評');

    await this.prisma.$transaction(async (tx) => {
      await tx.review.delete({ where: { id: reviewId } }); // cascades tags + images
      await this.recomputeAggregate(tx, review.restaurantId);
    });
    return { success: true };
  }

  /** Admin moderation: hide (exclude from public + aggregate) or restore. */
  async setVisibility(reviewId: number, hidden: boolean): Promise<ReviewItem> {
    const review = await this.prisma.review.findUnique({
      where: { id: reviewId },
      select: { id: true, restaurantId: true },
    });
    if (!review) throw ApiException.notFound('找不到這則雷評');

    const updated = await this.prisma.$transaction(async (tx) => {
      const r = await tx.review.update({
        where: { id: reviewId },
        data: { status: hidden ? ReviewStatus.HIDDEN : ReviewStatus.PUBLISHED },
        include: reviewInclude,
      });
      await this.recomputeAggregate(tx, review.restaurantId);
      return r;
    });
    return toReviewItem(updated);
  }

  private async ensureRestaurant(restaurantId: number) {
    const restaurant = await this.prisma.restaurant.findUnique({
      where: { id: restaurantId },
      select: { id: true, status: true },
    });
    if (!restaurant || restaurant.status === RestaurantStatus.HIDDEN) {
      throw ApiException.notFound('Restaurant not found');
    }
  }

  private async resolveTagIds(codes: string[]): Promise<number[]> {
    const unique = [...new Set(codes)];
    const tags = await this.prisma.reviewTag.findMany({ where: { code: { in: unique } } });
    if (tags.length !== unique.length) {
      const found = new Set(tags.map((t) => t.code));
      const missing = unique.filter((c) => !found.has(c));
      throw ApiException.validation(
        `Unknown tag code(s): ${missing.join(', ')}`,
        missing.map((c) => ({ field: 'tagCodes', message: `Unknown tag: ${c}` })),
      );
    }
    return tags.map((t) => t.id);
  }

  /** Recompute averageThunderScore + reviewCount from PUBLISHED reviews. */
  private async recomputeAggregate(tx: Prisma.TransactionClient, restaurantId: number) {
    const agg = await tx.review.aggregate({
      where: { restaurantId, status: ReviewStatus.PUBLISHED },
      _avg: { thunderScore: true },
      _count: true,
    });
    await tx.restaurant.update({
      where: { id: restaurantId },
      data: {
        averageThunderScore: agg._avg.thunderScore ? Number(agg._avg.thunderScore.toFixed(1)) : 0,
        reviewCount: agg._count,
      },
    });
  }
}
