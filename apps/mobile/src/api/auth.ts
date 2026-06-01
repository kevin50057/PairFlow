import { api, setToken } from './client';
import type { AuthResult, MyReviewItem, Page, UserProfile } from './types';

export async function register(email: string, password: string, nickname: string): Promise<UserProfile> {
  const res = await api.post<AuthResult>('/auth/register', { email, password, nickname });
  setToken(res.token);
  return res.user;
}

export async function login(email: string, password: string): Promise<UserProfile> {
  const res = await api.post<AuthResult>('/auth/login', { email, password });
  setToken(res.token);
  return res.user;
}

export const getMe = () => api.get<UserProfile>('/auth/me');

export const updateProfile = (data: { nickname?: string; avatarUrl?: string }) =>
  api.patch<UserProfile>('/users/me', data);

export const getMyReviews = (page = 0, size = 20) =>
  api.get<Page<MyReviewItem>>(`/users/me/reviews?page=${page}&size=${size}`);
