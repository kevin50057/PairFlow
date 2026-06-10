import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideChevronLeft, LucideChevronRight, LucideMapPin, LucidePlus, LucideTrash2, LucideX } from '@lucide/angular';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';
import { Anniversary, EventItem } from '../../core/models';

type Marker = { label: string; color: string };
type Cell = { key: string; day: number; inMonth: boolean; isToday: boolean; events: EventItem[]; markers: Marker[] };

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
  imports: [FormsModule, LucideChevronLeft, LucideChevronRight, LucideMapPin, LucidePlus, LucideTrash2, LucideX],
  template: `
    <div class="appbar">
      <h1>行事曆</h1>
      <button class="chip" (click)="goToday()">今天</button>
    </div>

    <div class="screen stack" style="padding-top:8px">
      <!-- month grid -->
      <div class="card" style="padding:12px">
        <div class="cal-head">
          <button class="cal-nav" (click)="shiftMonth(-1)" aria-label="上個月"><svg lucideChevronLeft size="20"></svg></button>
          <div class="cal-title">{{ monthLabel() }}</div>
          <button class="cal-nav" (click)="shiftMonth(1)" aria-label="下個月"><svg lucideChevronRight size="20"></svg></button>
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
                @for (e of c.events.slice(0, 4 - c.markers.length); track e.id) { <i class="dot" [style.background]="color(e.eventType)"></i> }
              </span>
            </button>
          }
        </div>

        <div class="cal-legend">
          @for (t of types; track t[0]) { <span class="leg"><i class="dot" [style.background]="t[2]"></i>{{ t[1] }}</span> }
        </div>
      </div>

      <!-- selected day detail -->
      <div class="card">
        <div class="between">
          <div class="section-title" style="margin:0">{{ selectedLabel() }}</div>
          <button class="chip" (click)="openCreate()"><svg lucidePlus size="15"></svg> 新增</button>
        </div>
        <hr class="dashed" />

        @for (m of selectedMarkers(); track $index) {
          <div class="row" style="margin:8px 0">
            <i class="dot lg" [style.background]="m.color"></i>
            <b class="grow">{{ m.label }}</b>
          </div>
        }
        @for (e of selectedEvents(); track e.id) {
          <div class="row" style="margin:10px 0;align-items:flex-start">
            <i class="dot lg" [style.background]="color(e.eventType)" style="margin-top:6px"></i>
            <div class="grow">
              <b>{{ e.title }}</b>
              <div class="tiny muted">{{ timeRange(e) }} · {{ typeLabel(e.eventType) }}</div>
              @if (e.locationName) { <div class="tiny muted"><svg lucideMapPin size="12"></svg> {{ e.locationName }}</div> }
              @if (e.description) { <div class="small" style="margin-top:2px">{{ e.description }}</div> }
            </div>
            <button class="icon-btn" (click)="remove(e)" aria-label="刪除"><svg lucideTrash2 size="16"></svg></button>
          </div>
        }
        @if (!selectedMarkers().length && !selectedEvents().length) {
          <p class="muted small" style="margin:6px 0">這天還沒有安排，點「新增」加一個吧 🌷</p>
        }
      </div>
    </div>

    <!-- create sheet -->
    @if (showCreate()) {
      <div class="sheet-backdrop" (click)="showCreate.set(false)"></div>
      <div class="sheet">
        <div class="between">
          <b>新增行程 · {{ selectedLabel() }}</b>
          <button class="icon-btn" (click)="showCreate.set(false)"><svg lucideX size="18"></svg></button>
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
            <div><div class="label">開始</div><input class="input" type="time" name="st" [(ngModel)]="f.startTime" /></div>
            <div><div class="label">結束（選填）</div><input class="input" type="time" name="et" [(ngModel)]="f.endTime" /></div>
          </div>
          <label class="row" style="gap:8px"><input type="checkbox" name="ad" [(ngModel)]="f.allDay" /> 整天</label>
          <input class="input" placeholder="地點（選填）" name="loc" [(ngModel)]="f.locationName" />
          <input class="input" placeholder="備註（選填）" name="desc" [(ngModel)]="f.description" />
          <button class="btn btn-primary btn-block" (click)="create()" [disabled]="saving()">{{ saving() ? '新增中…' : '新增行程' }}</button>
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

  viewMonth = signal(startOfMonth(new Date()));
  events = signal<EventItem[]>([]);
  annivs = signal<Anniversary[]>([]);
  selected = signal(dateKey(new Date()));
  showCreate = signal(false);
  saving = signal(false);
  f = { title: '', eventType: 'DATE', startTime: '', endTime: '', allDay: false, locationName: '', description: '' };

  monthLabel = computed(() => this.viewMonth().toLocaleDateString('zh-TW', { year: 'numeric', month: 'long' }));

  private eventsByDay = computed(() => {
    const map = new Map<string, EventItem[]>();
    for (const e of this.events()) {
      const k = dateKey(new Date(e.startTime));
      (map.get(k) ?? map.set(k, []).get(k)!).push(e);
    }
    for (const list of map.values()) list.sort((a, b) => +new Date(a.startTime) - +new Date(b.startTime));
    return map;
  });

  private recurring = computed<{ md: string; marker: Marker }[]>(() => {
    const out: { md: string; marker: Marker }[] = [];
    for (const a of this.annivs()) {
      out.push({ md: (a.date ?? '').slice(5), marker: { label: a.title, color: 'var(--danger)' } });
    }
    const me = this.auth.user();
    const partner = this.couple.couple()?.partner;
    for (const u of [me, partner]) {
      if (u?.birthday) out.push({ md: u.birthday.slice(5), marker: { label: u.displayName + ' 生日', color: BIRTHDAY_COLOR } });
    }
    return out;
  });

  private markersFor(key: string): Marker[] {
    const md = key.slice(5);
    return this.recurring().filter((r) => r.md === md).map((r) => r.marker);
  }

  grid = computed<Cell[]>(() => {
    const vm = this.viewMonth();
    const m = vm.getMonth();
    const start = new Date(vm); start.setDate(1 - vm.getDay());
    const last = new Date(vm.getFullYear(), m + 1, 0);
    const end = new Date(last); end.setDate(last.getDate() + (6 - last.getDay()));
    const evMap = this.eventsByDay();
    const todayKey = dateKey(new Date());
    const cells: Cell[] = [];
    for (const d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      const key = dateKey(d);
      cells.push({
        key, day: d.getDate(), inMonth: d.getMonth() === m, isToday: key === todayKey,
        events: evMap.get(key) ?? [], markers: this.markersFor(key),
      });
    }
    return cells;
  });

  selectedEvents = computed(() => this.eventsByDay().get(this.selected()) ?? []);
  selectedMarkers = computed(() => this.markersFor(this.selected()));
  selectedLabel = computed(() => {
    const d = new Date(this.selected() + 'T00:00:00');
    const k = this.selected() === dateKey(new Date()) ? '（今天）' : '';
    return d.toLocaleDateString('zh-TW', { month: 'long', day: 'numeric', weekday: 'long' }) + k;
  });

  ngOnInit() { this.load(); }

  async load() {
    const vm = this.viewMonth();
    const from = new Date(vm); from.setDate(1 - vm.getDay()); from.setHours(0, 0, 0, 0);
    const last = new Date(vm.getFullYear(), vm.getMonth() + 1, 0);
    const to = new Date(last); to.setDate(last.getDate() + (6 - last.getDay())); to.setHours(23, 59, 59);
    const [events, annivs] = await Promise.all([
      this.api.get<EventItem[]>('/events', { from: from.toISOString(), to: to.toISOString() }),
      this.api.get<Anniversary[]>('/anniversaries'),
    ]);
    this.events.set(events);
    this.annivs.set(annivs);
  }

  shiftMonth(delta: number) {
    const vm = this.viewMonth();
    this.viewMonth.set(new Date(vm.getFullYear(), vm.getMonth() + delta, 1));
    this.load();
  }
  goToday() {
    this.viewMonth.set(startOfMonth(new Date()));
    this.selected.set(dateKey(new Date()));
    this.load();
  }

  openCreate() {
    const now = new Date();
    this.f = { title: '', eventType: 'DATE', startTime: pad(now.getHours()) + ':00', endTime: '', allDay: false, locationName: '', description: '' };
    this.showCreate.set(true);
  }

  async create() {
    if (!this.f.title.trim()) return;
    this.saving.set(true);
    try {
      const start = new Date(this.selected() + 'T' + (this.f.allDay || !this.f.startTime ? '00:00' : this.f.startTime) + ':00');
      const body: Record<string, unknown> = {
        title: this.f.title.trim(), eventType: this.f.eventType, startTime: start.toISOString(),
        locationName: this.f.locationName || null, description: this.f.description || null,
      };
      if (!this.f.allDay && this.f.endTime) {
        body['endTime'] = new Date(this.selected() + 'T' + this.f.endTime + ':00').toISOString();
      }
      await this.api.post('/events', body);
      this.showCreate.set(false);
      await this.load();
    } finally {
      this.saving.set(false);
    }
  }

  async remove(e: EventItem) {
    await this.api.del(`/events/${e.id}`);
    await this.load();
  }

  color(type: string) { return TYPES.find((t) => t[0] === type)?.[2] ?? '#9aa0a6'; }
  typeLabel(type: string) { return TYPES.find((t) => t[0] === type)?.[1] ?? type; }
  timeRange(e: EventItem) {
    const s = new Date(e.startTime);
    if (s.getHours() === 0 && s.getMinutes() === 0 && !e.endTime) return '整天';
    const t = (d: Date) => d.toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit', hour12: false });
    return e.endTime ? `${t(s)}–${t(new Date(e.endTime))}` : t(s);
  }
}

function pad(n: number) { return String(n).padStart(2, '0'); }
function dateKey(d: Date) { return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`; }
function startOfMonth(d: Date) { return new Date(d.getFullYear(), d.getMonth(), 1); }
