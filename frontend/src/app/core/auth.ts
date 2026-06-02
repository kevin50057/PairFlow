import { Injectable, computed, inject, signal } from '@angular/core';
import { Api } from './api';
import { AuthResponse, User } from './models';

const TOKEN_KEY = 'pf_token';
const REFRESH_KEY = 'pf_refresh';

@Injectable({ providedIn: 'root' })
export class Auth {
  private api = inject(Api);

  readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  readonly user = signal<User | null>(null);
  readonly isAuthed = computed(() => !!this.token());

  async login(email: string, password: string): Promise<void> {
    this.setSession(await this.api.post<AuthResponse>('/auth/login', { email, password }));
  }

  async register(email: string, password: string, displayName: string): Promise<void> {
    this.setSession(await this.api.post<AuthResponse>('/auth/register', { email, password, displayName }));
  }

  /** Silently exchange the stored refresh token for a new access + refresh token pair. */
  async refreshTokens(): Promise<boolean> {
    const refreshToken = localStorage.getItem(REFRESH_KEY);
    if (!refreshToken) return false;
    try {
      const res = await this.api.post<AuthResponse>('/auth/refresh', { refreshToken });
      this.setSession(res);
      return true;
    } catch {
      this.clearSession();
      return false;
    }
  }

  async loadMe(): Promise<void> {
    this.user.set(await this.api.get<User>('/auth/me'));
  }

  async updateProfile(patch: Partial<User>): Promise<void> {
    this.user.set(await this.api.patch<User>('/users/me', patch));
  }

  async logout(): Promise<void> {
    const refreshToken = localStorage.getItem(REFRESH_KEY);
    try {
      await this.api.post('/auth/logout', refreshToken ? { refreshToken } : {});
    } catch { /* best-effort */ }
    this.clearSession();
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY);
  }

  private setSession(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    if (res.refreshToken) localStorage.setItem(REFRESH_KEY, res.refreshToken);
    this.token.set(res.token);
    this.user.set(res.user);
  }

  private clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    this.token.set(null);
    this.user.set(null);
  }
}
