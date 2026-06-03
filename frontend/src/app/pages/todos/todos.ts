import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucidePlus, LucideX } from '@lucide/angular';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';
import { Todo } from '../../core/models';
import { initial } from '../../core/labels';
import { CoupleAvatar } from '../../shared/couple-avatar';

@Component({
  selector: 'pf-todos',
  imports: [FormsModule, RouterLink, CoupleAvatar, LucidePlus, LucideX],
  template: `
    <div class="appbar">
      <div>
        <h1>我們的任務</h1>
        <div class="subtitle">把今天要照顧的事排清楚</div>
      </div>
      <pf-couple-avatar [size]="38" />
    </div>

    <div class="screen-pad-sm" style="padding-bottom:0">
      <div class="filterbar">
        @for (t of tabs; track t[0]) {
          <button class="filter-pill" [class.active]="tab() === t[0]" (click)="setTab(t[0])">
            {{ t[1] }}
          </button>
        }
      </div>
    </div>

    <div class="screen stack" style="padding-top:12px">
      @if (showCreate()) {
        <div class="card stack">
          <input class="input" placeholder="新增任務…" name="title" [(ngModel)]="f.title" />
          <div class="grid2">
            <select class="input" name="assignee" [(ngModel)]="f.assignee">
              <option value="BOTH">一起</option><option value="ME">我</option><option value="PARTNER">對方</option><option value="UNASSIGNED">未指派</option>
            </select>
            <select class="input" name="priority" [(ngModel)]="f.priority">
              <option value="LOW">低</option><option value="MEDIUM">中</option><option value="HIGH">高</option>
            </select>
          </div>
          <div class="grid2">
            <select class="input" name="type" [(ngModel)]="f.type">
              <option value="GENERAL">一般</option><option value="DATE">約會</option><option value="TRAVEL">旅行</option>
              <option value="HOUSEWORK">家務</option><option value="SHOPPING">採買</option><option value="GOAL">目標</option><option value="SURPRISE">驚喜</option>
            </select>
            <input class="input" type="datetime-local" name="due" [(ngModel)]="f.dueDate" />
          </div>
          <div class="row">
            <button class="btn btn-primary grow" (click)="create()"><svg lucidePlus size="18"></svg>新增</button>
            <button class="btn btn-outline" (click)="showCreate.set(false)"><svg lucideX size="18"></svg>取消</button>
          </div>
        </div>
      }

      @if (loading()) {
        <div class="empty">載入中…</div>
      } @else if (!todos().length) {
        <div class="empty">這個分頁還沒有任務<br />點右下角新增一件一起完成的事</div>
      } @else {
        <div class="card">
          <div class="between">
            <h3 style="margin:0">{{ tabLabel() }}</h3>
            @if (tab() === 'today') { <span class="tiny" style="color:var(--primary-ink)">📅 {{ todayLabel }}</span> }
          </div>
          <hr class="dashed" />
          <div class="pill-list">
            @for (t of todos(); track t.id) {
              <div class="row">
                <span class="todo-check" [class.done]="t.status === 'DONE'" (click)="toggle(t)">@if (t.status === 'DONE') { ✓ }</span>
                <a class="grow" [routerLink]="['/todos', t.id]">
                  <div [class.strike]="t.status === 'DONE'" style="font-weight:700">{{ t.title }}
                    @if (t.isSecret) { <span class="badge badge-soft">驚喜</span> }
                  </div>
                  <div class="row tiny" style="margin-top:4px;gap:6px">
                    <span class="tag">{{ assignee(t) }}</span>
                    @if (t.dueDate) { <span class="muted">截止 {{ date(t.dueDate) }}</span> }
                    @if (t.priority === 'HIGH') { <span style="color:var(--danger)">· 高</span> }
                  </div>
                  @if (t.type === 'GOAL' && t.goalTarget) {
                    <div class="tiny" style="color:var(--primary-ink);margin-top:2px">{{ t.goalCurrent || 0 }} / {{ t.goalTarget }} {{ t.goalUnit }}</div>
                  }
                </a>
                <span style="font-size:1.15rem">🩷</span>
              </div>
            }
          </div>
        </div>
      }
    </div>
    <button class="fab" (click)="showCreate.set(!showCreate())" aria-label="新增任務"><svg lucidePlus size="28"></svg></button>
  `,
})
export class TodosPage implements OnInit {
  private api = inject(Api);
  private auth = inject(Auth);
  private couple = inject(CoupleStore);

  tabs: [string, string, string][] = [
    ['today', '今天', '💗'], ['undated', '想到再做', '💭'], ['DATE', '約會', '💕'],
    ['TRAVEL', '旅行', '✈️'], ['HOUSEWORK', '家務', '🏠'], ['GOAL', '目標', '🚩'],
    ['done', '已完成', '✅'], ['week', '本週', '🗓️'], ['all', '全部', '📋'],
  ];
  tab = signal('today');
  todos = signal<Todo[]>([]);
  loading = signal(false);
  showCreate = signal(false);
  f = { title: '', type: 'GENERAL', assignee: 'BOTH', priority: 'MEDIUM', dueDate: '' };
  todayLabel = new Date().toLocaleDateString('zh-TW', { month: 'numeric', day: 'numeric', weekday: 'long' });

  ngOnInit() { this.load(); }

  setTab(t: string) { this.tab.set(t); this.load(); }
  tabLabel() { return this.tabs.find((t) => t[0] === this.tab())?.[1] ?? '任務'; }
  meInitial() { return initial(this.auth.user()?.displayName); }
  partnerInitial() { return initial(this.couple.couple()?.partner?.displayName); }

  async load() {
    this.loading.set(true);
    try { this.todos.set(await this.api.get<Todo[]>('/todos', this.params())); }
    finally { this.loading.set(false); }
  }

  private params(): Record<string, string> {
    const t = this.tab();
    const start = new Date(); start.setHours(0, 0, 0, 0);
    const iso = (d: Date) => d.toISOString();
    const plus = (days: number) => { const d = new Date(start); d.setDate(d.getDate() + days); return d; };
    if (t === 'today') return { dueFrom: iso(start), dueTo: iso(plus(1)) };
    if (t === 'week') return { dueFrom: iso(start), dueTo: iso(plus(7)) };
    if (t === 'undated') return { undated: 'true' };
    if (t === 'done') return { status: 'DONE' };
    if (['DATE', 'TRAVEL', 'HOUSEWORK', 'GOAL'].includes(t)) return { type: t };
    return {};
  }

  async create() {
    if (!this.f.title.trim()) return;
    const body: Record<string, unknown> = {
      title: this.f.title.trim(), type: this.f.type, assignee: this.f.assignee, priority: this.f.priority,
    };
    if (this.f.dueDate) body['dueDate'] = new Date(this.f.dueDate).toISOString();
    await this.api.post('/todos', body);
    this.f = { title: '', type: 'GENERAL', assignee: 'BOTH', priority: 'MEDIUM', dueDate: '' };
    this.showCreate.set(false);
    this.load();
  }

  async toggle(t: Todo) {
    if (t.status === 'DONE') await this.api.patch(`/todos/${t.id}`, { status: 'PENDING' });
    else await this.api.post(`/todos/${t.id}/complete`);
    this.load();
  }

  assignee(t: Todo) { return t.assignee === 'me' ? '我' : t.assignee === 'partner' ? '對方' : t.assignee === 'both' ? '一起' : '未指派'; }
  date(iso: string) { return new Date(iso).toLocaleDateString('zh-TW', { month: 'numeric', day: 'numeric' }); }
}
