import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { NoteItem } from '../../core/models';

@Component({
  selector: 'pf-notes',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">小紙條</h1>
      <button class="btn btn-ghost btn-sm" (click)="showCompose.set(!showCompose())">寫一張</button>
    </div>
    <div class="screen stack">
      @if (showCompose()) {
        <div class="card stack">
          <input class="input" placeholder="標題（選填）" name="t" [(ngModel)]="f.title" />
          <textarea class="textarea" placeholder="想對另一半說的話…" name="c" [(ngModel)]="f.content"></textarea>
          <div class="grid2">
            <select class="input" name="ty" [(ngModel)]="f.noteType">
              <option value="NOTE">小紙條</option><option value="LETTER">信</option><option value="FUTURE">未來信</option>
              <option value="APOLOGY">道歉</option><option value="THANKS">感謝</option><option value="ENCOURAGE">鼓勵</option><option value="ANNIVERSARY_CARD">紀念卡</option>
            </select>
            <input class="input" type="datetime-local" name="ut" [(ngModel)]="f.unlockTime" title="定時解鎖（選填）" />
          </div>
          <p class="tiny muted">設定解鎖時間，就是一封到指定時間才會出現的未來信 ✨</p>
          <button class="btn btn-primary btn-block" [disabled]="!f.content.trim()" (click)="post()">送出</button>
        </div>
      }

      @if (!notes().length) { <div class="empty">還沒有紙條，寫第一張給對方吧 ✉️</div> }
      @for (n of notes(); track n.id) {
        <div class="card">
          <div class="between">
            <b class="small">{{ mine(n) ? '我寫的' : '給我的' }} · {{ typeLabel(n.noteType) }}</b>
            <span class="tiny muted">{{ date(n.createdAt) }}</span>
          </div>
          @if (n.locked) {
            <p class="muted small" style="margin-top:6px">🔒 未來信，將於 {{ date(n.unlockTime!) }} 解鎖</p>
          } @else {
            @if (n.title) { <div style="margin-top:6px;font-weight:600">{{ n.title }}</div> }
            <p style="margin-top:4px;white-space:pre-wrap">{{ n.content }}</p>
          }
          <div class="row" style="margin-top:8px">
            @if (!mine(n) && !n.isRead && !n.locked) { <button class="btn btn-ghost btn-sm" (click)="read(n)">標記已讀</button> }
            @if (n.isRead && !mine(n)) { <span class="tiny muted">已讀</span> }
            <button class="chip" (click)="fav(n)">{{ n.isFavorite ? '★ 已收藏' : '☆ 收藏' }}</button>
          </div>
        </div>
      }
    </div>
  `,
})
export class NotesPage implements OnInit {
  private api = inject(Api);
  private auth = inject(Auth);
  loc = inject(Location);

  notes = signal<NoteItem[]>([]);
  showCompose = signal(false);
  f = { title: '', content: '', noteType: 'NOTE', unlockTime: '' };

  ngOnInit() { this.load(); }
  async load() { this.notes.set(await this.api.get<NoteItem[]>('/notes')); }

  async post() {
    if (!this.f.content.trim()) return;
    const body: Record<string, unknown> = { title: this.f.title || null, content: this.f.content.trim(), noteType: this.f.noteType };
    if (this.f.unlockTime) body['unlockTime'] = new Date(this.f.unlockTime).toISOString();
    await this.api.post('/notes', body);
    this.f = { title: '', content: '', noteType: 'NOTE', unlockTime: '' };
    this.showCompose.set(false);
    this.load();
  }

  async read(n: NoteItem) { await this.api.post(`/notes/${n.id}/read`); this.load(); }
  async fav(n: NoteItem) { await this.api.post(`/notes/${n.id}/favorite`); this.load(); }

  mine(n: NoteItem) { return n.senderId === this.auth.user()?.id; }
  typeLabel(t: string) {
    const m: Record<string, string> = { NOTE: '小紙條', LETTER: '信', FUTURE: '未來信', APOLOGY: '道歉', THANKS: '感謝', ENCOURAGE: '鼓勵', ANNIVERSARY_CARD: '紀念卡' };
    return m[t] ?? t;
  }
  date(iso: string) { return new Date(iso).toLocaleString('zh-TW', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false }); }
}
