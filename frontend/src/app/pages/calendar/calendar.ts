import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideChevronLeft, LucideChevronRight, LucideClock, LucideMapPin, LucidePencil, LucidePlus, LucideTrash2, LucideX } from '@lucide/angular';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';
import { Anniversary, EventItem, Wish } from '../../core/models';

type Marker = { label: string; color: string };
type Cell = { key: string; day: number; inMonth: boolean; isToday: boolean; events: EventItem[]; wishes: Wish[]; markers: Marker[] };

const TYPES: [string, string, string][] = [
  ['DATE', '約會', 'var(--primary)'],
  ['TRAVEL', '旅行', 'var(--teal)'],
  ['ANNIVERSARY', '紀念日', 'var(--danger)'],
  ['HOUSEWORK', '家務', 'var(--peach)'],
  ['PERSONAL', '個人', 'var(--lavender)'],
  ['OTHER', '其他', '#9aa0a6'],
];
const BIRTHDAY_COLOR = '#e8a13a';

@Component({
  selector: 'pf-calendar',
  imports: [FormsModule, LucideChevronLeft, LucideChevronRight, LucideClock, LucideMapPin, LucidePencil, LucidePlus, LucideTrash2, LucideX],
  template: `
    <div class="appbar">
      <h1>行事曆</h1>
      <div class="row" style="gap:6px">
        <div class="seg" style="width:auto">
          <button class="seg-btn" [class.sel]="view() === 'month'" (click)="setView('month')">月</button>
          <button class="seg-btn" [class.sel]="view() === 'week'" (click)="setView('week')">週</button>
        </div>
        <button class="chip" (click)="goToday()">今天</button>
      </div>
    </div>

    <div class="screen stack" style="padding-top:8px">
      <div class="card" style="padding:12px">
        <div class="cal-head">
          <button class="cal-nav" (click)="shift(-1)" aria-label="上一頁"><svg lucideChevronLeft size="20"></svg></button>
          <div class="cal-title">{{ periodLabel() }}</div>
          <button class="cal-nav" (click)="shift(1)" aria-label="下一頁"><svg lucideChevronRight size="20"></svg></button>
        </div>

        <div class="cal-grid cal-dow">
          @for (d of dow; track d) { <div class="dow" [class.we]="d === '日' || d === '六'">{{ d }}</div> }
        </div>
        <div class="cal-grid">
          @for (c of grid(); track c.key) {
            <button class="cal-cell" [class.dim]="!c.inMonth" [class.today]="c.isToday"
                    [class.sel]="c.key === selected()" (click)="selected.set(c.key)" type="button">
              <span class="cal-d">{{ c.day }}</span>
              <span class="cal-dots">
                @for (m of c.markers; track $index) { <i class="dot" [style.background]="m.color"></i> }
                @for (e of c.events.slice(0, 3); track e.id) { <i class="dot" [style.background]="color(e.eventType)"></i> }
                @for (w of c.wishes.slice(0, 2); track w.id) { <span class="wish-pip" [class.done]="w.status === 'COMPLETED'">💗</span> }
              </span>
            </button>
          }
        </div>

        <div class="cal-legend">
          @for (t of types; track t[0]) { <span class="leg"><i class="dot" [style.background]="t[2]"></i>{{ t[1] }}</span> }
          <span class="leg"><span class="wish-pip">💗</span>願望</span>
        </div>
      </div>

      <!-- selected day agenda -->
      <div class="card">
        <div class="between">
          <div class="section-title" style="margin:0">{{ selectedLabel() }}</div>
          <button class="chip" (click)="openCreate()"><svg lucidePlus size="15"></svg> 新增</button>
        </div>
        <hr class="dashed" />

        @for (m of selectedMarkers(); track $index) {
          <div class="row" style="margin:8px 0"><i class="dot lg" [style.background]="m.color"></i><b class="grow">{{ m.label }}</b></div>
        }
        @for (e of selectedEvents(); track e.id) {
          <div class="row" style="margin:10px 0;align-items:flex-start" (click)="openEdit(e)">
            <i class="dot lg" [style.background]="color(e.eventType)" style="margin-top:6px"></i>
            <div class="grow">
              <b>{{ e.title }}</b>
              <div class="tiny muted">{{ timeRange(e) }} · {{ typeLabel(e.eventType) }}</div>
              @if (e.locationName) { <div class="tiny muted"><svg lucideMapPin size="12"></svg> {{ e.locationName }}</div> }
            </div>
            <button class="icon-btn" (click)="$event.stopPropagation(); openEdit(e)" aria-label="編輯"><svg lucidePencil size="15"></svg></button>
            <button class="icon-btn" (click)="$event.stopPropagation(); removeEvent(e)" aria-label="刪除"><svg lucideTrash2 size="15"></svg></button>
          </div>
        }
        @for (w of selectedWishes(); track w.id) {
          <div class="row" style="margin:10px 0;align-items:flex-start">
            <span style="font-size:1.05rem;margin-top:1px">💗</span>
            <div class="grow">
              <b [class.strike]="w.status === 'COMPLETED'">{{ w.title }}</b>
              <div class="tiny muted row" style="gap:6px">
                <span>{{ wishTime(w) }} · 願望</span>
                @if (w.status === 'COMPLETED') { <span class="badge badge-soft">已完成</span> }
                @else { <span class="auto-badge"><svg lucideClock size="11"></svg> 到時間自動完成</span> }
              </div>
            </div>
          </div>
        }
        @if (!selectedMarkers().length && !selectedEvents().length && !selectedWishes().length) {
          <p class="muted small" style="margin:6px 0">這天還沒有安排，點「新增」加一個吧 🌷</p>
        }
      </div>
    </div>

    <!-- create / edit sheet -->
    @if (showSheet()) {
      <div class="sheet-backdrop" (click)="showSheet.set(false)"></div>
      <div class="sheet">
        <div class="between">
          <b>{{ editId() ? '編輯行程' : '新增行程' }} · {{ selectedLabel() }}</b>
          <button class="icon-btn" (click)="showSheet.set(false)"><svg lucideX size="18"></svg></button>
        </div>
        <div class="stack" style="margin-top:12px">
          <input class="input" placeholder="行程標題" name="t" [(ngModel)]="f.title" />
          <div>
            <div class="label">類別</div>
            <div class="type-pick">
              @for (t of types; track t[0]) {
                <button class="type-chip" type="button" [class.sel]="f.eventType === t[0]" (click)="f.eventType = t[0]"
                        [style.--c]="t[2]"><i class="dot" [style.background]="t[2]"></i>{{ t[1] }}</button>
              }
            </div>
          </div>
          <div class="grid2">
            <div><div class="label">開始</div><input class="input" type="time" name="st" [(ngModel)]="f.startTime" [disabled]="f.allDay" /></div>
            <div><div class="label">結束（選填）</div><input class="input" type="time" name="et" [(ngModel)]="f.endTime" [disabled]="f.allDay" /></div>
          </div>
          <label class="row" style="gap:8px"><input type="checkbox" name="ad" [(ngModel)]="f.allDay" /> 整天</label>
          <input class="input" placeholder="地點（選填）" name="loc" [(ngModel)]="f.locationName" />
          <input class="input" placeholder="備註（選填）" name="desc" [(ngModel)]="f.description" />
          <button class="btn btn-primary btn-block" (click)="save()" [disabled]="saving()">{{ saving() ? '儲存中…' : (editId() ? '儲存' : '新增行程') }}</button>
        </div>
      </div>
    }
  `,
})
export class CalendarPage implements OnInit {
  private api = inject(Api);
  private auth = inject(Auth);
  private couple = inject(CoupleStore);

  dow = ['日', '一', '二', '三', '四', '五', '六'];
  types = TYPES;

  view = signal<'month' | 'week'>('month');
  cursor = signal(new Date());
  events = signal<EventItem[]>([]);
  wishes = signal<Wish[]>([]);
  annivs = signal<Anniversary[]>([]);
  selected = signal(dateKey(new Date()));
  showSheet = signal(false);
  editId = signal<string | null>(null);
  saving = signal(false);
  f = { title: '', eventType: 'DATE', startTime: '', endTime: '', allDay: false, locationName: '', description: '' };

  private range = computed<[Date, Date]>(() => {
    const c = this.cursor();
    if (this.view() === 'week') {
      const start = new Date(c); start.setDate(c.getDate() - c.getDay()); start.setHours(0, 0, 0, 0);
      const end = new Date(start); end.setDate(start.getDate() + 6); end.setHours(23, 59, 59);
      return [start, end];
    }
    const first = new Date(c.getFullYear(), c.getMonth(), 1);
    const start = new Date(first); start.setDate(1 - first.getDay());
    const last = new Date(c.getFullYear(), c.getMonth() + 1, 0);
    const end = new Date(last); end.setDate(last.getDate() + (6 - last.getDay())); end.setHours(23, 59, 59);
    return [start, end];
  });

  periodLabel = computed(() => {
    const c = this.cursor();
    if (this.view() === 'month') return c.toLocaleDateString('zh-TW', { year: 'numeric', month: 'long' });
    const [s, e] = this.range();
    return `${s.toLocaleDateString('zh-TW', { month: 'long', day: 'numeric' })}–${e.toLocaleDateString('zh-TW', { month: 'numeric', day: 'numeric' })}`;
  });

  private eventsByDay = computed(() => groupByDay(this.events(), (e) => e.startTime));
  private wishesByDay = computed(() => groupByDay(this.wishes(), (w) => w.scheduledAt));

  private recurring = computed<{ md: string; marker: Marker }[]>(() => {
    const out: { md: string; marker: Marker }[] = [];
    for (const a of this.annivs()) out.push({ md: (a.date ?? '').slice(5), marker: { label: a.title, color: 'var(--danger)' } });
    for (const u of [this.auth.user(), this.couple.couple()?.partner]) {
      if (u?.birthday) out.push({ md: u.birthday.slice(5), marker: { label: u.displayName + ' 生日', color: BIRTHDAY_COLOR } });
    }
    return out;
  });
  private markersFor(key: string): Marker[] {
    const md = key.slice(5);
    return this.recurring().filter((r) => r.md === md).map((r) => r.marker);
  }

  grid = computed<Cell[]>(() => {
    const [start, end] = this.range();
    const month = this.cursor().getMonth();
    const evMap = this.eventsByDay();
    const wMap = this.wishesByDay();
    const todayKey = dateKey(new Date());
    const cells: Cell[] = [];
    for (const d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      const key = dateKey(d);
      cells.push({
        key, day: d.getDate(), inMonth: this.view() === 'week' || d.getMonth() === month,
        isToday: key === todayKey, events: evMap.get(key) ?? [], wishes: wMap.get(key) ?? [], markers: this.markersFor(key),
      });
    }
    return cells;
  });

  selectedEvents = computed(() => this.eventsByDay().get(this.selected()) ?? []);
  selectedWishes = computed(() => this.wishesByDay().get(this.selected()) ?? []);
  selectedMarkers = computed(() => this.markersFor(this.selected()));
  selectedLabel = computed(() => {
    const d = new Date(this.selected() + 'T00:00:00');
    const today = this.selected() === dateKey(new Date()) ? '（今天）' : '';
    return d.toLocaleDateString('zh-TW', { month: 'long', day: 'numeric', weekday: 'long' }) + today;
  });

  ngOnInit() { this.load(); }

  async load() {
    const [from, to] = this.range();
    const [events, wishes, annivs] = await Promise.all([
      this.api.get<EventItem[]>('/events', { from: from.toISOString(), to: to.toISOString() }),
      this.api.get<Wish[]>('/wishes'),
      this.api.get<Anniversary[]>('/anniversaries'),
    ]);
    this.events.set(events);
    this.wishes.set(wishes);
    this.annivs.set(annivs);
  }

  setView(v: 'month' | 'week') {
    if (this.view() === v) return;
    this.cursor.set(new Date(this.selected() + 'T00:00:00'));
    this.view.set(v);
    this.load();
  }
  shift(delta: number) {
    const c = this.cursor();
    this.cursor.set(this.view() === 'week'
      ? new Date(c.getFullYear(), c.getMonth(), c.getDate() + delta * 7)
      : new Date(c.getFullYear(), c.getMonth() + delta, 1));
    this.load();
  }
  goToday() {
    this.cursor.set(new Date());
    this.selected.set(dateKey(new Date()));
    this.load();
  }

  openCreate() {
    const now = new Date();
    this.editId.set(null);
    this.f = { title: '', eventType: 'DATE', startTime: pad(now.getHours()) + ':00', endTime: '', allDay: false, locationName: '', description: '' };
    this.showSheet.set(true);
  }
  openEdit(e: EventItem) {
    this.selected.set(dateKey(new Date(e.startTime)));
    this.editId.set(e.id);
    const s = new Date(e.startTime);
    const allDay = s.getHours() === 0 && s.getMinutes() === 0 && !e.endTime;
    this.f = {
      title: e.title, eventType: e.eventType, allDay,
      startTime: allDay ? '' : hhmm(s), endTime: e.endTime ? hhmm(new Date(e.endTime)) : '',
      locationName: e.locationName ?? '', description: e.description ?? '',
    };
    this.showSheet.set(true);
  }

  async save() {
    if (!this.f.title.trim()) return;
    this.saving.set(true);
    try {
      const start = new Date(this.selected() + 'T' + (this.f.allDay || !this.f.startTime ? '00:00' : this.f.startTime) + ':00');
      const body: Record<string, unknown> = {
        title: this.f.title.trim(), eventType: this.f.eventType, startTime: start.toISOString(),
        locationName: this.f.locationName || null, description: this.f.description || null,
        endTime: !this.f.allDay && this.f.endTime ? new Date(this.selected() + 'T' + this.f.endTime + ':00').toISOString() : null,
      };
      if (this.editId()) await this.api.patch(`/events/${this.editId()}`, body);
      else await this.api.post('/events', body);
      this.showSheet.set(false);
      await this.load();
    } finally {
      this.saving.set(false);
    }
  }

  async removeEvent(e: EventItem) { await this.api.del(`/events/${e.id}`); await this.load(); }

  color(type: string) { return TYPES.find((t) => t[0] === type)?.[2] ?? '#9aa0a6'; }
  typeLabel(type: string) { return TYPES.find((t) => t[0] === type)?.[1] ?? type; }
  timeRange(e: EventItem) {
    const s = new Date(e.startTime);
    if (s.getHours() === 0 && s.getMinutes() === 0 && !e.endTime) return '整天';
    return e.endTime ? `${hhmm(s)}–${hhmm(new Date(e.endTime))}` : hhmm(s);
  }
  wishTime(w: Wish) {
    if (!w.scheduledAt) return '';
    const d = new Date(w.scheduledAt);
    return d.getHours() === 0 && d.getMinutes() === 0 ? '整天' : hhmm(d);
  }
}

function pad(n: number) { return String(n).padStart(2, '0'); }
function hhmm(d: Date) { return d.toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit', hour12: false }); }
function dateKey(d: Date) { return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`; }
function groupByDay<T>(items: T[], getIso: (x: T) => string | undefined): Map<string, T[]> {
  const map = new Map<string, T[]>();
  for (const it of items) {
    const iso = getIso(it);
    if (!iso) continue;
    const k = dateKey(new Date(iso));
    (map.get(k) ?? map.set(k, []).get(k)!).push(it);
  }
  return map;
}
