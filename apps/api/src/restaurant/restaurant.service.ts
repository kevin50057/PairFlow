import { Injectable } from '@nestjs/common';
import { Restaurant } from '@prisma/client';
import { ApiException } from '../common/api-error';
import { buildPage, Page } from '../common/dto/pagination';
import { RestaurantStatus, ReviewStatus } from '../common/enums';
import { boundingBox, haversineMeters } from '../common/geo';
import { PrismaService } from '../prisma/prisma.service';
import { reviewInclude, toReviewItem } from '../review/review.mapper';
import { NearbyQuery } from './dto/nearby-query.dto';
import { SearchQuery } from './dto/search-query.dto';
import { RestaurantCard, RestaurantDetail, TagCount } from './dto/restaurant.dto';

// Upper bounds for in-memory work; plenty for MVP scale.
const NEARBY_SCAN_LIMIT = 500;
const SEARCH_SCAN_LIMIT = 100;
const LATEST_REVIEWS = 5;

@Injectable()
export class RestaurantService {
  constructor(private readonly prisma: PrismaService) {}

  /** Active restaurants within `radius`, nearest first. Distance via Haversine. */
  async nearby(q: NearbyQuery): Promise<Page<RestaurantCard>> {
    const box = boundingBox(q.lat, q.lng, q.radius);
    const candidates = await this.prisma.restaurant.findMany({
      where: {
        status: RestaurantStatus.ACTIVE,
        latitude: { gte: box.minLat, lte: box.maxLat },
        longitude: { gte: box.minLng, lte: box.maxLng },
      },
      take: NEARBY_SCAN_LIMIT,
    });

    const ranked = candidates
      .map((r) => ({ r, distance: haversineMeters(q.lat, q.lng, r.latitude, r.longitude) }))
      .filter((x) => x.distance <= q.radius)
      .sort((a, b) => a.distance - b.distance);

    const start = q.page * q.size;
    const slice = ranked.slice(start, start + q.size);
    return buildPage(
      slice.map(({ r, distance }) => this.toCard(r, distance)),
      q.page,
      q.size,
      ranked.length > start + q.size,
    );
  }

  /** Name search with exact > prefix > substring ranking. */
  async search(q: SearchQuery): Promise<Page<RestaurantCard>> {
    const matches = await this.prisma.restaurant.findMany({
      where: { status: RestaurantStatus.ACTIVE, name: { contains: q.q } },
      take: SEARCH_SCAN_LIMIT,
    });

    const keyword = q.q.toLowerCase();
    const ranked = matches
      .map((r) => ({ r, rank: this.matchRank(r.name.toLowerCase(), keyword) }))
      .sort((a, b) => a.rank - b.rank || b.r.reviewCount - a.r.reviewCount);

    const start = q.page * q.size;
    const slice = ranked.slice(start, start + q.size);
    return buildPage(
      slice.map(({ r }) => this.toCard(r, null)),
      q.page,
      q.size,
      ranked.length > start + q.size,
    );
  }

  async detail(id: number): Promise<RestaurantDetail> {
    const restaurant = await this.prisma.restaurant.findUnique({ where: { id } });
    if (!restaurant || restaurant.status === RestaurantStatus.HIDDEN) {
      throw ApiException.notFound('Restaurant not found');
    }

    const reviews = await this.prisma.review.findMany({
      where: { restaurantId: id, status: ReviewStatus.PUBLISHED },
      orderBy: { createdAt: 'desc' },
      take: LATEST_REVIEWS,
      include: reviewInclude,
    });

    return {
      ...this.toDetailBase(restaurant),
      tagDistribution: await this.tagDistribution(id),
      latestReviews: reviews.map(toReviewItem),
    };
  }

  private async tagDistribution(restaurantId: number): Promise<TagCount[]> {
    const rows = await this.prisma.reviewTagRel.findMany({
      where: { review: { restaurantId, status: ReviewStatus.PUBLISHED } },
      include: { reviewTag: true },
    });
    const counts = new Map<string, TagCount>();
    for (const row of rows) {
      const existing = counts.get(row.reviewTag.code);
      if (existing) existing.count += 1;
      else counts.set(row.reviewTag.code, { code: row.reviewTag.code, label: row.reviewTag.label, count: 1 });
    }
    return [...counts.values()].sort((a, b) => b.count - a.count);
  }

  private matchRank(name: string, keyword: string): number {
    if (name === keyword) return 0;
    if (name.startsWith(keyword)) return 1;
    return 2;
  }

  private toCard(r: Restaurant, distanceMeters: number | null): RestaurantCard {
    return {
      id: r.id,
      name: r.name,
      address: r.address,
      latitude: r.latitude,
      longitude: r.longitude,
      distanceMeters,
      averageThunderScore: r.averageThunderScore,
      reviewCount: r.reviewCount,
      coverImageUrl: r.coverImageUrl,
      category: r.category,
      district: r.district,
    };
  }

  private toDetailBase(r: Restaurant) {
    return {
      id: r.id,
      name: r.name,
      address: r.address,
      latitude: r.latitude,
      longitude: r.longitude,
      phone: r.phone,
      category: r.category,
      city: r.city,
      district: r.district,
      coverImageUrl: r.coverImageUrl,
      averageThunderScore: r.averageThunderScore,
      reviewCount: r.reviewCount,
      status: r.status,
    };
  }
}
