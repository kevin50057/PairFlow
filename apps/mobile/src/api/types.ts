// API response shapes (mirror of the backend DTOs). Kept as a thin contract so
// the UI stays decoupled from how the server stores data. A future improvement
// is to extract these into a shared workspace package consumed by both apps.

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  hasNext: boolean;
}

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

export interface ReviewTagRef {
  code: string;
  label: string;
}

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
  tags: ReviewTagRef[];
  imageUrls: string[];
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

export interface ReviewTag {
  code: string;
  label: string;
  sortOrder: number;
}

export interface CreateReviewPayload {
  thunderScore: number;
  title?: string;
  content: string;
  visitDate?: string;
  tagCodes: string[];
  imageUrls?: string[];
}

export interface ApiErrorBody {
  code: string;
  message: string;
  details: unknown[];
}

export interface UserProfile {
  id: number;
  email: string;
  nickname: string;
  avatarUrl: string | null;
  role: string;
  status: string;
  createdAt: string;
}

export interface AuthResult {
  token: string;
  user: UserProfile;
}

export interface MyReviewItem extends ReviewItem {
  restaurant: {
    id: number;
    name: string;
    coverImageUrl: string | null;
    averageThunderScore: number;
  };
}

export interface ReportReasonOption {
  code: string;
  label: string;
}

export const REPORT_REASONS: ReportReasonOption[] = [
  { code: 'ABUSE', label: '辱罵/人身攻擊' },
  { code: 'SPAM', label: '廣告/洗版' },
  { code: 'FAKE', label: '不實/假評論' },
  { code: 'SEXUAL_VIOLENT', label: '色情/暴力' },
  { code: 'IRRELEVANT', label: '與餐廳無關' },
];

export function reportReasonLabel(code: string): string {
  return REPORT_REASONS.find((r) => r.code === code)?.label ?? code;
}

export interface AdminReportItem {
  id: number;
  targetType: string;
  targetId: number;
  reporterUserId: number;
  reasonCode: string;
  note: string | null;
  status: string;
  createdAt: string;
  target: {
    id: number;
    content: string;
    status: string;
    restaurantId: number;
    author: { id: number; nickname: string };
  } | null;
}
