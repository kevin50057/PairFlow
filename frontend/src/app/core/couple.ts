import { Injectable, inject, signal } from '@angular/core';
import { Api } from './api';
import { Couple } from './models';

@Injectable({ providedIn: 'root' })
export class CoupleStore {
  private api = inject(Api);

  readonly couple = signal<Couple | null>(null);
  readonly loaded = signal(false);

  /** Loads the active couple; a 404 simply means "not paired yet". */
  async load(): Promise<Couple | null> {
    try {
      const c = await this.api.get<Couple>('/couples/me');
      this.couple.set(c);
      return c;
    } catch {
      this.couple.set(null);
      return null;
    } finally {
      this.loaded.set(true);
    }
  }

  createInvite(): Promise<{ code: string; expiresAt: string }> {
    return this.api.post('/couples/invite');
  }

  async join(code: string): Promise<Couple> {
    const c = await this.api.post<Couple>('/couples/join', { code });
    this.couple.set(c);
    return c;
  }

  async setStartDate(date: string): Promise<void> {
    const c = this.couple();
    if (!c) return;
    this.couple.set(await this.api.patch<Couple>(`/couples/${c.id}`, { relationshipStartDate: date }));
  }
}
