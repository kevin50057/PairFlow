import { api } from './client';
import type {
  CreateReviewPayload,
  Page,
  RestaurantCard,
  RestaurantDetail,
  ReviewItem,
  ReviewTag,
} from './types';

export const getNearby = (lat: number, lng: number, radius = 1000, page = 0, size = 20) =>
  api.get<Page<RestaurantCard>>(
    `/restaurants/nearby?lat=${lat}&lng=${lng}&radius=${radius}&page=${page}&size=${size}`,
  );

export const searchRestaurants = (q: string, page = 0, size = 20) =>
  api.get<Page<RestaurantCard>>(`/restaurants/search?q=${encodeURIComponent(q)}&page=${page}&size=${size}`);

export const getRestaurant = (id: number | string) => api.get<RestaurantDetail>(`/restaurants/${id}`);

export const getReviews = (id: number | string, page = 0, size = 20) =>
  api.get<Page<ReviewItem>>(`/restaurants/${id}/reviews?page=${page}&size=${size}`);

export const createReview = (id: number | string, payload: CreateReviewPayload) =>
  api.post<ReviewItem>(`/restaurants/${id}/reviews`, payload);

export const getReview = (reviewId: number | string) => api.get<ReviewItem>(`/reviews/${reviewId}`);

export const updateReview = (reviewId: number | string, payload: CreateReviewPayload) =>
  api.put<ReviewItem>(`/reviews/${reviewId}`, payload);

export const deleteReview = (reviewId: number | string) =>
  api.delete<{ success: boolean }>(`/reviews/${reviewId}`);

export const reportReview = (reviewId: number | string, reasonCode: string, note?: string) =>
  api.post<{ id: number; status: string }>(`/reports`, {
    targetType: 'REVIEW',
    targetId: Number(reviewId),
    reasonCode,
    note,
  });

export const uploadReviewImage = (file: File) => {
  const form = new FormData();
  form.append('file', file);
  return api.upload<{ url: string }>(`/media/review-images`, form);
};

export const getReviewTags = () => api.get<ReviewTag[]>(`/review-tags`);
