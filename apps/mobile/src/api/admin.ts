import { api } from './client';
import type { AdminReportItem, Page, ReviewItem } from './types';

export const getAdminReports = (status: string | undefined, page = 0, size = 20) =>
  api.get<Page<AdminReportItem>>(
    `/admin/reports?${status ? `status=${status}&` : ''}page=${page}&size=${size}`,
  );

export const hideReview = (reviewId: number) => api.patch<ReviewItem>(`/admin/reviews/${reviewId}/hide`, {});

export const restoreReview = (reviewId: number) => api.patch<ReviewItem>(`/admin/reviews/${reviewId}/restore`, {});

export const resolveReport = (reportId: number, status: 'RESOLVED' | 'REJECTED') =>
  api.patch<{ id: number; status: string }>(`/admin/reports/${reportId}`, { status });
