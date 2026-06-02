import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';
import { Wish } from '../../core/models';
import { WISH_CATEGORY, initial } from '../../core/labels';

@Component({
  selector: 'pf-wishlist',
  imports: [FormsModule],
  template: `
    <div class="appbar">
      <button class="back" (click)="loc.back()">‹</button>
      <h1 style="font-size:1.25rem">未來一起做的事</h1>
      <button class="btn btn-ghost btn-sm" (click)="show.set(!show())">＋ 新增</button>
    </div>

    <div class="screen stack">
      <!-- progress card -->
      <div class="card">
        <div class="row">
          <div class="wish-thumb" style="width:54px;height:54px;font-size:1.7rem;background:linear-gradient(160deg,#ffd0e0,#f79ab8)">💗</div>
          <div class="grow">
            <div style="font-weight:800;font-size:1.05rem">願望清單 <span style="color:var(--primary-deep)">{{ completed() }}</span> / {{ total() }} 已完成</div>
            <div class="subtitle small">一起把想做的事慢慢完成 ♡</div>
          </div>
          <span class="couple-cartoon" style="font-size:3rem">💑</span>
        </div>
        <div class="row" style="margin-top:14px;gap:10px">
          <div class="progress-track grow"><div class="progress-fill" [style.width.%]="pct()"></div></div>
          <b style="color:var(--primary-deep)">{{ pct() }}%</b>
        </div>
      </div>

      @if (show()) {
        <div class="card stack">
          <input class="input" placeholder="想一起做的事…" name="t" [(ngModel)]="f.title" />
          <div class="grid2">
            <select class="input" name="c" [(ngModel)]="f.category">
              @for (c of cats; track c[0]) { <option [value]="c[0]">{{ c[1] }}</option> }
            </select>
            <input class="input" placeholder="目標時間（例：2026 冬天）" name="tn" [(ngModel)]="f.targetNote" />
          </div>
          <input class="input" placeholder="一句話描述（選填）" name="d" [(ngModel)]="f.description" />
          <button class="btn btn-primary btn-block" [disabled]="!f.title.trim()" (click)="create()">加入願望</button>
        </div>
      }

      <!-- category filter -->
      <div class="chip-row">
        <span class="chip" [class.active]="!selectedCat()" (click)="selectedCat.set(null)">全部</span>
        @for (c of cats; track c[0]) {
          <span class="chip" [class.active]="selectedCat() === c[0]" (click)="selectedCat.set(c[0])">{{ c[1] }}</span>
        }
      </div>

      <!-- list -->
      @if (!filtered().length) {
        <div class="empty"><span class="emoji">🎆</span>還沒有願望<br />一起看煙火、一起看極光、一起去那家店…</div>
      }
      @for (w of filtered(); track w.id) {
        <div class="card" [class.done-card]="w.status === 'COMPLETED'">
          <div class="wishrow">
            <div class="wish-thumb" [style.background]="thumb(w.category).g">{{ thumb(w.category).e }}</div>
            <div class="grow">
              <div class="between">
                <b [class.strike]="w.status === 'COMPLETED'">{{ w.title }}</b>
                @if (w.status === 'COMPLETED') { <span class="badge badge-soft">已完成</span> }
              </div>
              <span class="tag" style="margin-top:5px;display:inline-block">{{ catLabel(w.category) }}</span>
              @if (w.targetNote) { <div class="tiny muted" style="margin-top:5px">📅 {{ w.targetNote }}</div> }
              @if (w.description) { <div class="small muted" style="margin-top:2px">{{ w.description }}</div> }
              <div class="row tiny" style="margin-top:7px;gap:12px">
                @if (w.status !== 'COMPLETED' && !w.convertedTodoId) { <a class="link" (click)="toTodo(w)">轉成任務</a> }
                <a (click)="remove(w)" style="color:var(--muted);cursor:pointer">刪除</a>
              </div>
            </div>
            <button class="heart-toggle" (click)="toggleDone(w)" [attr.aria-label]="w.status === 'COMPLETED' ? '取消完成' : '完成'">
              {{ w.status === 'COMPLETED' ? '♥' : '♡' }}
            </button>
          </div>
        </div>
      }
    </div>
  `,
})
export class WishlistPage implements OnInit {
  private api = inject(Api);
  loc = inject(Location);
  private auth = inject(Auth);
  private couple = inject(CoupleStore);

  wishes = signal<Wish[]>([]);
  selectedCat = signal<string | null>(null);
  show = signal(false);
  cats = Object.entries(WISH_CATEGORY);
  f = { title: '', category: 'PLACE', targetNote: '', description: '' };

  private readonly thumbs: Record<string, { e: string; g: string }> = {
    PLACE: { e: '🏔️', g: 'linear-gradient(160deg,#bcd7ff,#8fb4f0)' },
    FOOD: { e: '🍜', g: 'linear-gradient(160deg,#ffd9a8,#ffb877)' },
    MOVIE: { e: '🎬', g: 'linear-gradient(160deg,#dcc7ff,#b79bf0)' },
    BUY: { e: '🎁', g: 'linear-gradient(160deg,#ffc9dd,#f79ab8)' },
    DO: { e: '🎆', g: 'linear-gradient(160deg,#c7b6ff,#9a86e8)' },
    LEARN: { e: '📚', g: 'linear-gradient(160deg,#bdeacb,#86cf9f)' },
    OTHER: { e: '✨', g: 'linear-gradient(160deg,#ffd6e6,#f7a8c4)' },
  };

  total = computed(() => this.wishes().length);
  completed = computed(() => this.wishes().filter((w) => w.status === 'COMPLETED').length);
  pct = computed(() => (this.total() ? Math.round((this.completed() / this.total()) * 100) : 0));
  filtered = computed(() => {
    const c = this.selectedCat();
    return c ? this.wishes().filter((w) => w.category === c) : this.wishes();
  });

  ngOnInit() { this.load(); }
  async load() { this.wishes.set(await this.api.get<Wish[]>('/wishes')); }

  meInitial() { return initial(this.auth.user()?.displayName); }
  partnerInitial() { return initial(this.couple.couple()?.partner?.displayName); }
  catLabel(c: string) { return WISH_CATEGORY[c] ?? c; }
  thumb(c: string) { return this.thumbs[c] ?? this.thumbs['OTHER']; }

  async create() {
    if (!this.f.title.trim()) return;
    await this.api.post('/wishes', {
      title: this.f.title.trim(), category: this.f.category,
      targetNote: this.f.targetNote || null, description: this.f.description || null,
    });
    this.f = { title: '', category: 'PLACE', targetNote: '', description: '' };
    this.show.set(false);
    this.load();
  }

  async toggleDone(w: Wish) {
    if (w.status === 'COMPLETED') await this.api.patch(`/wishes/${w.id}`, { status: 'ACTIVE' });
    else await this.api.post(`/wishes/${w.id}/complete`);
    this.load();
  }
  async toTodo(w: Wish) { await this.api.post(`/wishes/${w.id}/to-todo`); this.load(); }
  async remove(w: Wish) { await this.api.del(`/wishes/${w.id}`); this.load(); }
}
