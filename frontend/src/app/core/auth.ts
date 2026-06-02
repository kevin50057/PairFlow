import { Injectable, computed, inject, signal } from '@angular/core';
import { Api } from './api';
import { AuthResponse, User } from './models';

const TOKEN_KEY = 'pf_token';

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

  async loadMe(): Promise<void> {
    this.user.set(await this.api.get<User>('/auth/me'));
  }

  async updateProfile(patch: Partial<User>): Promise<void> {
    this.user.set(await this.api.patch<User>('/users/me', patch));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.token.set(null);
    this.user.set(null);
  }

  private setSession(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    this.token.set(res.token);
    this.user.set(res.user);
  }
}
