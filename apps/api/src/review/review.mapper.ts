import { Prisma } from '@prisma/client';

// Single source of truth for how a review is loaded and shaped for the API.
// Shared by both the restaurant detail endpoint and the review endpoints.
export const reviewInclude = {
  user: { select: { id: true, nickname: true } },
  tags: { include: { reviewTag: true } },
  images: { orderBy: { sortOrder: 'asc' as const } },
} satisfies Prisma.ReviewInclude;

export type ReviewWithRelations = Prisma.ReviewGetPayload<{ include: typeof reviewInclude }>;

export interface ReviewItem {
  id: number;
  restaurantId: number;
  thunderScore: number;
  title: string | null;
  content: string;
  visitDate: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
  author: { id: number; nickname: string };
  tags: { code: string; label: string }[];
  imageUrls: string[];
}

export function toReviewItem(rv: ReviewWithRelations): ReviewItem {
  return {
    id: rv.id,
    restaurantId: rv.restaurantId,
    thunderScore: rv.thunderScore,
    title: rv.title,
    content: rv.content,
    visitDate: rv.visitDate ? rv.visitDate.toISOString().slice(0, 10) : null,
    status: rv.status,
    createdAt: rv.createdAt.toISOString(),
    updatedAt: rv.updatedAt.toISOString(),
    author: { id: rv.user.id, nickname: rv.user.nickname },
    tags: rv.tags.map((t) => ({ code: t.reviewTag.code, label: t.reviewTag.label })),
    imageUrls: rv.images.map((i) => i.fileUrl),
  };
}
