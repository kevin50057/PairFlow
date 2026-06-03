import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideBell, LucideCalendarDays, LucideClock, LucideHeart, LucideListTodo, LucideSmilePlus, LucideSparkles } from '@lucide/angular';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';
import { Home, Todo, Wish } from '../../core/models';
import { MOOD, initial } from '../../core/labels';
import { NotificationStore } from '../../core/notifications';
import { CoupleAvatar } from '../../shared/couple-avatar';

@Component({
  selector: 'pf-home',
  imports: [
    RouterLink,
    CoupleAvatar,
    LucideBell,
    LucideCalendarDays,
    LucideClock,
    LucideHeart,
    LucideListTodo,
    LucideSmilePlus,
    LucideSparkles,
  ],
  template: `
    <div class="appbar">
      <div>
        <h1>Hi {{ auth.user()?.displayName }}</h1>
        @if (home()?.couple?.daysTogether != null) {
          <div class="subtitle">你們在一起第 <b style="color:var(--primary-ink)">{{ home()!.couple.daysTogether }}</b> 天</div>
        }
      </div>
      <div class="row" style="gap:10px">
        <a routerLink="/us/notifications" class="bell" aria-label="通知"><svg lucideBell size="20"></svg>@if (notif.unread() > 0) { <span class="bell-badge">{{ notif.unread() }}</span> }</a>
        <pf-couple-avatar [size]="38" />
      </div>
    </div>

    <div class="screen stack">
      @if (loading()) {
        <div class="empty">載入中…</div>
      } @else {
        <div class="card daily-panel">
          <div class="daily-kicker">Today together</div>
          <div class="daily-title">{{ dailyHeadline() }}</div>
          <div class="summary-grid">
            <a class="summary-item" routerLink="/todos">
              <div class="metric-row"><span>今日任務</span><svg lucideListTodo size="18"></svg></div>
              <b>{{ home()?.todayTodos?.length || 0 }}</b>
            </a>
            <a class="summary-item" routerLink="/calendar">
              <div class="metric-row"><span>今日行程</span><svg lucideCalendarDays size="18"></svg></div>
              <b>{{ home()?.todayEvents?.length || 0 }}</b>
            </a>
            <a class="summary-item" routerLink="/us/mood">
              <div class="metric-row"><span>對方心情</span><svg lucideSmilePlus size="18"></svg></div>
              <b style="font-size:1rem">{{ home()?.partnerMood ? label(home()!.partnerMood!.mood) : '未更新' }}</b>
            </a>
            <a class="summary-item" routerLink="/calendar">
              <div class="metric-row"><span>下一紀念日</span><svg lucideClock size="18"></svg></div>
              <b>{{ home()?.nextAnniversary?.daysLeft ?? '-' }}</b>
            </a>
          </div>
        </div>

        <!-- partner mood -->
        <div class="card card-warm">
          <div class="section-title" style="margin-top:0">對方今日心情</div>
          @if (home()?.partnerMood; as m) {
            <div class="row">
              <div style="font-size:2rem">{{ emoji(m.mood) }}</div>
              <div class="grow">
                <b>{{ couple.couple()?.partner?.displayName }}</b> 今天：{{ label(m.mood) }}
                @if (m.note) { <div class="muted small">{{ m.note }}</div> }
              </div>
            </div>
            <div class="row wrap" style="margin-top:10px">
              <button class="chip" (click)="react('HUG')"><svg class="icon-inline" lucideHeart></svg> 抱抱你</button>
              <button class="chip" (click)="react('LATER')"><svg class="icon-inline" lucideClock></svg> 晚點陪你</button>
              <a class="chip" routerLink="/us/notes"><svg class="icon-inline" lucideSparkles></svg> 傳小紙條</a>
            </div>
          } @else {
            <p class="muted small">對方今天還沒有打卡心情。<a class="link" routerLink="/us/mood">分享你的心情</a></p>
          }
        </div>

        <!-- today todos -->
        <div class="card">
          <div class="between">
            <div class="section-title" style="margin-top:0">今日任務</div>
            <a class="link small" routerLink="/todos">全部</a>
          </div>
          @if (home()?.todayTodos?.length) {
            <div class="pill-list">
              @for (t of home()!.todayTodos; track t.id) {
                <div class="row">
                  <span class="todo-check" [class.done]="t.status === 'DONE'" (click)="toggle(t)">
                    @if (t.status === 'DONE') { ✓ }
                  </span>
                  <a class="grow" [routerLink]="['/todos', t.id]" [class.strike]="t.status === 'DONE'">{{ t.title }}</a>
                  <span class="tag">{{ assignee(t) }}</span>
                </div>
              }
            </div>
          } @else {
            <p class="muted small">今天沒有共同任務，輕鬆一下 🌿</p>
          }
        </div>

        <!-- next anniversary -->
        @if (home()?.nextAnniversary; as a) {
          <div class="card">
            <div class="section-title" style="margin-top:0">下一個重要日子</div>
            <div class="between">
              <div><b>{{ a.title }}</b><div class="muted small">{{ a.date }}</div></div>
              <div class="center-text"><div class="hero-day" style="font-size:1.5rem">{{ a.daysLeft }}</div><div class="tiny muted">天後</div></div>
            </div>
          </div>
        }

        <!-- today events -->
        @if (home()?.todayEvents?.length) {
          <div class="card">
            <div class="section-title" style="margin-top:0">今日行程</div>
            @for (e of home()!.todayEvents; track e.id) {
              <div class="row" style="margin-top:6px"><b style="color:var(--primary-ink)">{{ time(e.startTime) }}</b><span>{{ e.title }}</span></div>
            }
          </div>
        }

        <!-- memory -->
        @if (home()?.memory; as mem) {
          <div class="card card-warm">
            <div class="section-title" style="margin-top:0">{{ mem.title }}</div>
            <p style="margin:0">{{ mem.description }}</p>
          </div>
        }

        <!-- AI nudges -->
        @if (nudges().length) {
          <div class="card">
            <div class="section-title" style="margin-top:0">AI 小提醒</div>
            @for (n of nudges(); track n) { <p class="small" style="margin:4px 0"><svg class="icon-inline" lucideSparkles></svg> {{ n }}</p> }
          </div>
        }

        <a class="card card-warm" routerLink="/us/wishlist" style="display:block">
          <div class="between"><div class="section-title" style="margin-top:0">未來一起做的事</div><span class="link small">查看</span></div>
          @if (wishes().length) {
            @for (w of wishes().slice(0, 3); track w.id) { <div class="row"><span>🎆</span><span>{{ w.title }}</span></div> }
            @if (wishes().length > 3) { <div class="tiny muted" style="margin-top:4px">還有 {{ wishes().length - 3 }} 件…</div> }
          } @else {
            <p class="muted small" style="margin:0">一起看煙火、一起看極光… 列下你們想一起做的事 ✨</p>
          }
        </a>
      }
    </div>
  `,
})
export class HomePage implements OnInit {
  private api = inject(Api);
  auth = inject(Auth);
  couple = inject(CoupleStore);
  notif = inject(NotificationStore);

  home = signal<Home | null>(null);
  nudges = signal<string[]>([]);
  wishes = signal<Wish[]>([]);
  loading = signal(true);

  async ngOnInit() {
    await this.reload();
    this.loadNudges();
    this.loadWishes();
    this.notif.refresh();
  }

  meInitial() { return initial(this.auth.user()?.displayName); }
  partnerInitial() { return initial(this.couple.couple()?.partner?.displayName); }

  async reload() {
    this.loading.set(true);
    try {
      this.home.set(await this.api.get<Home>('/home'));
    } finally {
      this.loading.set(false);
    }
  }

  async loadNudges() {
    try {
      this.nudges.set((await this.api.get<{ items: string[] }>('/ai/nudges')).items);
    } catch { /* non-critical */ }
  }

  async loadWishes() {
    try {
      this.wishes.set(await this.api.get<Wish[]>('/wishes', { status: 'ACTIVE' }));
    } catch { /* non-critical */ }
  }

  label(code: string) { return MOOD[code]?.label ?? code; }
  emoji(code: string) { return MOOD[code]?.emoji ?? '🙂'; }
  assignee(t: Todo) { return t.assignee === 'me' ? '我' : t.assignee === 'partner' ? '對方' : t.assignee === 'both' ? '一起' : ''; }
  time(iso: string) { return new Date(iso).toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit', hour12: false }); }
  dailyHeadline() {
    const todos = this.home()?.todayTodos?.length || 0;
    const events = this.home()?.todayEvents?.length || 0;
    if (todos || events) return `今天有 ${todos} 件任務、${events} 個行程要一起照顧`;
    if (this.home()?.partnerMood) return '今天先從關心彼此的狀態開始';
    return '今天沒有排程，留一點空白給彼此';
  }

  async react(type: string) {
    const m = this.home()?.partnerMood;
    if (!m) return;
    try { await this.api.post(`/moods/${m.id}/reactions`, { reaction: type }); await this.reload(); } catch { /* ignore */ }
  }

  async toggle(t: Todo) {
    try {
      if (t.status === 'DONE') {
        await this.api.patch(`/todos/${t.id}`, { status: 'PENDING' });
      } else {
        await this.api.post(`/todos/${t.id}/complete`);
      }
      await this.reload();
    } catch { /* ignore */ }
  }
}
