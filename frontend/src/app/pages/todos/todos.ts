import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Api } from '../../core/api';
import { Todo } from '../../core/models';

@Component({
  selector: 'pf-todos',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="appbar"><h1>任務</h1></div>
    <div class="screen-pad-sm">
      <div class="chip-row">
        @for (t of tabs; track t[0]) {
          <span class="chip" [class.active]="tab() === t[0]" (click)="setTab(t[0])">{{ t[1] }}</span>
        }
      </div>
    </div>

    <div class="screen stack" style="padding-top:0">
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
          <div class="row"><button class="btn btn-primary grow" (click)="create()">新增</button><button class="btn btn-outline" (click)="showCreate.set(false)">取消</button></div>
        </div>
      }

      @if (loading()) { <div class="empty">載入中…</div> }
      @else if (!todos().length) { <div class="empty">這裡還沒有任務 ✨</div> }
      @else {
        <div class="pill-list">
          @for (t of todos(); track t.id) {
            <div class="card" style="padding:12px 14px">
              <div class="row">
                <span class="todo-check" [class.done]="t.status === 'DONE'" (click)="toggle(t)">@if (t.status === 'DONE') { ✓ }</span>
                <a class="grow" [routerLink]="['/todos', t.id]">
                  <div [class.strike]="t.status === 'DONE'">{{ t.title }}
                    @if (t.isSecret) { <span class="badge badge-soft">驚喜</span> }
                  </div>
                  <div class="tiny muted">
                    {{ assignee(t) }}
                    @if (t.dueDate) { ｜截止 {{ date(t.dueDate) }} }
                    @if (t.priority === 'HIGH') { ｜<span style="color:var(--danger)">高</span> }
                  </div>
                  @if (t.type === 'GOAL' && t.goalTarget) {
                    <div class="tiny" style="color:var(--primary-ink)">{{ t.goalCurrent || 0 }} / {{ t.goalTarget }} {{ t.goalUnit }}</div>
                  }
                </a>
              </div>
            </div>
          }
        </div>
      }
    </div>
    <button class="fab" (click)="showCreate.set(!showCreate())">＋</button>
  `,
})
export class TodosPage implements OnInit {
  private api = inject(Api);

  tabs: [string, string][] = [
    ['today', '今天'], ['week', '本週'], ['undated', '想到再做'], ['DATE', '約會'], ['TRAVEL', '旅行'],
    ['HOUSEWORK', '家務'], ['GOAL', '目標'], ['done', '已完成'], ['all', '全部'],
  ];
  tab = signal('today');
  todos = signal<Todo[]>([]);
  loading = signal(false);
  showCreate = signal(false);
  f = { title: '', type: 'GENERAL', assignee: 'BOTH', priority: 'MEDIUM', dueDate: '' };

  ngOnInit() { this.load(); }

  setTab(t: string) { this.tab.set(t); this.load(); }

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
