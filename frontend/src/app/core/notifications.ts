import { Injectable, inject, signal } from '@angular/core';
import { Api } from './api';

/** Shared unread-notification count for the bell badge + nav dot. */
@Injectable({ providedIn: 'root' })
export class NotificationStore {
  private api = inject(Api);
  readonly unread = signal(0);

  async refresh(): Promise<void> {
    try {
      const r = await this.api.get<{ count: number }>('/notifications/unread-count');
      this.unread.set(r.count ?? 0);
    } catch {
      /* ignore */
    }
  }
}
