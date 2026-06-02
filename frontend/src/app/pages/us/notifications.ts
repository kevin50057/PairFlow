import { Component, OnInit, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { Api } from '../../core/api';
import { Notification } from '../../core/models';

const TYPE_LABEL: Record<string, string> = {
  TODO_DUE: '任務到期', TODO_CREATED: '新任務', TODO_COMPLETED: '任務完成', ANNIVERSARY: '紀念日',
  NOTE: '小紙條', LETTER_UNLOCK: '未來信解鎖', MOOD: '心情', EVENT: '行程', DAILY_QUESTION: '每日問答', AI_NUDGE: 'AI 提醒',
};

@Component({
  selector: 'pf-notifications',
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">通知</h1>
      <button class="btn btn-ghost btn-sm" (click)="markAll()">全部已讀</button>
    </div>
    <div class="screen stack">
      @if (!items().length) { <div class="empty"><span class="emoji">🔔</span>目前沒有通知</div> }
      @for (n of items(); track n.id) {
        <div class="card" [style.opacity]="n.isRead ? 0.6 : 1" (click)="markRead(n)" style="cursor:pointer">
          <div class="between">
            <b class="small">@if (!n.isRead) { <span style="color:var(--primary)">●</span> } {{ n.title }}</b>
            <span class="tiny muted">{{ date(n.createdAt) }}</span>
          </div>
          @if (n.body) { <p class="small muted" style="margin:4px 0 0">{{ n.body }}</p> }
        </div>
      }

      <div class="section-title">通知設定</div>
      <div class="card">
        @for (p of prefList(); track p[0]) {
          <label class="between" style="padding:8px 0;cursor:pointer">
            <span class="small">{{ typeLabel(p[0]) }}</span>
            <input type="checkbox" [checked]="p[1]" (change)="toggle(p[0])" />
          </label>
        }
      </div>
    </div>
  `,
})
export class NotificationsPage implements OnInit {
  private api = inject(Api);
  loc = inject(Location);

  items = signal<Notification[]>([]);
  prefs = signal<Record<string, boolean>>({});

  ngOnInit() { this.load(); }
  async load() {
    this.items.set(await this.api.get<Notification[]>('/notifications'));
    this.prefs.set(await this.api.get<Record<string, boolean>>('/notifications/preferences'));
  }

  prefList() { return Object.entries(this.prefs()); }

  async markRead(n: Notification) {
    if (n.isRead) return;
    await this.api.post(`/notifications/${n.id}/read`);
    this.load();
  }
  async markAll() { await this.api.post('/notifications/read-all'); this.load(); }

  async toggle(type: string) {
    const next = { ...this.prefs(), [type]: !this.prefs()[type] };
    this.prefs.set(next);
    const disabled = Object.entries(next).filter(([, on]) => !on).map(([t]) => t);
    await this.api.put('/notifications/preferences', { disabledTypes: disabled });
  }

  typeLabel(t: string) { return TYPE_LABEL[t] ?? t; }
  date(iso: string) { return new Date(iso).toLocaleString('zh-TW', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false }); }
}
