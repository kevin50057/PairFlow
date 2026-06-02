import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../../core/api';
import { Anniversary, EventItem } from '../../core/models';

@Component({
  selector: 'pf-calendar',
  imports: [FormsModule],
  template: `
    <div class="appbar"><h1>行事曆</h1><span class="muted small">{{ monthLabel }}</span></div>
    <div class="screen stack">
      @if (annivs().length) {
        <div class="card">
          <div class="section-title" style="margin-top:0">紀念日 / 倒數</div>
          <div class="chip-row">
            @for (a of annivs(); track a.id) {
              <span class="chip">{{ a.title }} · {{ a.daysLeft }} 天</span>
            }
          </div>
        </div>
      }

      <div class="card">
        <div class="section-title" style="margin-top:0">本月行程</div>
        @if (!events().length) { <p class="muted small">這個月還沒有行程</p> }
        @for (e of events(); track e.id) {
          <div class="row" style="margin:8px 0">
            <div class="center-text" style="width:46px">
              <div style="font-weight:800;color:var(--primary-ink)">{{ day(e.startTime) }}</div>
              <div class="tiny muted">{{ time(e.startTime) }}</div>
            </div>
            <div class="grow">
              <b>{{ e.title }}</b>
              @if (e.locationName) { <div class="tiny muted">📍 {{ e.locationName }}</div> }
            </div>
          </div>
        }
      </div>

      @if (showCreate()) {
        <div class="card stack">
          <input class="input" placeholder="行程標題" name="t" [(ngModel)]="f.title" />
          <div class="grid2">
            <select class="input" name="ty" [(ngModel)]="f.eventType">
              <option value="DATE">約會</option><option value="TRAVEL">旅行</option><option value="ANNIVERSARY">紀念日</option>
              <option value="HOUSEWORK">家務</option><option value="PERSONAL">個人</option><option value="OTHER">其他</option>
            </select>
            <input class="input" type="datetime-local" name="st" [(ngModel)]="f.startTime" />
          </div>
          <input class="input" placeholder="地點（選填）" name="loc" [(ngModel)]="f.locationName" />
          <div class="row"><button class="btn btn-primary grow" (click)="create()">新增行程</button><button class="btn btn-outline" (click)="showCreate.set(false)">取消</button></div>
        </div>
      }
    </div>
    <button class="fab" (click)="showCreate.set(!showCreate())">＋</button>
  `,
})
export class CalendarPage implements OnInit {
  private api = inject(Api);
  events = signal<EventItem[]>([]);
  annivs = signal<Anniversary[]>([]);
  showCreate = signal(false);
  f = { title: '', eventType: 'DATE', startTime: '', locationName: '' };
  monthLabel = new Date().toLocaleDateString('zh-TW', { year: 'numeric', month: 'long' });

  ngOnInit() { this.load(); }

  async load() {
    const now = new Date();
    const from = new Date(now.getFullYear(), now.getMonth(), 1);
    const to = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59);
    this.events.set(await this.api.get<EventItem[]>('/events', { from: from.toISOString(), to: to.toISOString() }));
    this.annivs.set(await this.api.get<Anniversary[]>('/anniversaries'));
  }

  async create() {
    if (!this.f.title.trim() || !this.f.startTime) return;
    await this.api.post('/events', {
      title: this.f.title.trim(), eventType: this.f.eventType,
      startTime: new Date(this.f.startTime).toISOString(), locationName: this.f.locationName || null,
    });
    this.f = { title: '', eventType: 'DATE', startTime: '', locationName: '' };
    this.showCreate.set(false);
    this.load();
  }

  day(iso: string) { return new Date(iso).toLocaleDateString('zh-TW', { day: 'numeric' }); }
  time(iso: string) { return new Date(iso).toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit', hour12: false }); }
}
