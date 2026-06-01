import { ReviewItem } from '../../review/review.mapper';

// Response shapes (DTOs) kept separate from Prisma entities.

export interface RestaurantCard {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  distanceMeters: number | null;
  averageThunderScore: number;
  reviewCount: number;
  coverImageUrl: string | null;
  category: string | null;
  district: string | null;
}

export interface TagCount {
  code: string;
  label: string;
  count: number;
}

export interface RestaurantDetail {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  phone: string | null;
  category: string | null;
  city: string | null;
  district: string | null;
  coverImageUrl: string | null;
  averageThunderScore: number;
  reviewCount: number;
  status: string;
  tagDistribution: TagCount[];
  latestReviews: ReviewItem[];
}
