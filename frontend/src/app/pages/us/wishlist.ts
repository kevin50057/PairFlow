import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Api } from '../../core/api';
import { Wish } from '../../core/models';
import { WISH_CATEGORY } from '../../core/labels';

@Component({
  selector: 'pf-wishlist',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">未來一起做的事</h1>
      <button class="btn btn-ghost btn-sm" (click)="show.set(!show())">＋ 新增</button>
    </div>
    <div class="screen stack">
      @if (show()) {
        <div class="card stack">
          <input class="input" placeholder="想一起做的事…" name="t" [(ngModel)]="f.title" />
          <div class="grid2">
            <select class="input" name="c" [(ngModel)]="f.category">
              @for (c of cats; track c[0]) { <option [value]="c[0]">{{ c[1] }}</option> }
            </select>
            <input class="input" type="number" placeholder="預估花費" name="cost" [(ngModel)]="f.estimatedCost" />
          </div>
          <input class="input" placeholder="地點 / 連結（選填）" name="loc" [(ngModel)]="f.location" />
          <button class="btn btn-primary btn-block" [disabled]="!f.title.trim()" (click)="create()">加入願望</button>
        </div>
      }

      @if (!wishes().length) { <div class="empty">列下你們想一起做的事吧 ✨<br />一起看煙火、一起看極光、一起去那家店…</div> }
      @for (w of wishes(); track w.id) {
        <div class="card">
          <div class="between">
            <div>
              <b [class.strike]="w.status === 'COMPLETED'">{{ w.title }}</b>
              <div class="tiny muted">{{ cat(w.category) }}@if (w.estimatedCost) { ・約 {{ w.estimatedCost }} 元 }@if (w.location) { ・{{ w.location }} }</div>
            </div>
            @if (w.status === 'COMPLETED') { <span class="badge badge-soft">已完成</span> }
          </div>
          <div class="row wrap" style="margin-top:10px">
            @if (w.status !== 'COMPLETED') {
              <button class="chip" (click)="complete(w)">✓ 完成</button>
              @if (!w.convertedTodoId) { <button class="chip" (click)="toTodo(w)">→ 轉成任務</button> }
              @else { <span class="tiny muted">已建立任務</span> }
            }
            <button class="chip" (click)="remove(w)">刪除</button>
          </div>
        </div>
      }
    </div>
  `,
})
export class WishlistPage implements OnInit {
  private api = inject(Api);
  loc = inject(Location);

  wishes = signal<Wish[]>([]);
  show = signal(false);
  cats = Object.entries(WISH_CATEGORY);
  f = { title: '', category: 'PLACE', estimatedCost: null as number | null, location: '' };

  ngOnInit() { this.load(); }
  async load() { this.wishes.set(await this.api.get<Wish[]>('/wishes')); }

  async create() {
    if (!this.f.title.trim()) return;
    await this.api.post('/wishes', {
      title: this.f.title.trim(), category: this.f.category,
      estimatedCost: this.f.estimatedCost || null, location: this.f.location || null,
    });
    this.f = { title: '', category: 'PLACE', estimatedCost: null, location: '' };
    this.show.set(false);
    this.load();
  }

  async complete(w: Wish) { await this.api.post(`/wishes/${w.id}/complete`); this.load(); }
  async toTodo(w: Wish) { await this.api.post(`/wishes/${w.id}/to-todo`); this.load(); }
  async remove(w: Wish) { await this.api.del(`/wishes/${w.id}`); this.load(); }
  cat(c: string) { return WISH_CATEGORY[c] ?? c; }
}
