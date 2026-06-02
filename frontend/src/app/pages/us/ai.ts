import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Api } from '../../core/api';

@Component({
  selector: 'pf-ai',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">AI 助手</h1><span></span></div>
    <div class="screen stack">
      <div class="card stack">
        <div class="section-title" style="margin-top:0">把計畫拆成任務</div>
        <input class="input" placeholder="例如：週末要去台中兩天一夜" name="bd" [(ngModel)]="bd" />
        <button class="btn btn-ghost btn-block" (click)="breakdown()">拆解</button>
        @for (t of bdItems(); track t) { <div class="row"><span>•</span><span>{{ t }}</span></div> }
      </div>

      <div class="card stack">
        <div class="section-title" style="margin-top:0">約會點子</div>
        <button class="btn btn-ghost btn-block" (click)="suggest()">給我一些建議</button>
        @for (s of sugItems(); track s) { <div class="row"><span>💡</span><span>{{ s }}</span></div> }
      </div>

      <div class="card stack">
        <div class="section-title" style="margin-top:0">紀念日卡片文字</div>
        <input class="input" placeholder="場合，例如：交往一週年" name="occ" [(ngModel)]="occ" />
        <button class="btn btn-ghost btn-block" (click)="message()">幫我寫一段</button>
        @if (occRes()) { <p style="white-space:pre-wrap">{{ occRes() }}</p> }
      </div>

      <div class="card stack">
        <div class="section-title" style="margin-top:0">把話說得溫柔一點</div>
        <textarea class="textarea" placeholder="貼上你想說但怕太衝的話…" name="sf" [(ngModel)]="sf"></textarea>
        <button class="btn btn-ghost btn-block" (click)="soften()">潤飾</button>
        @if (sfRes(); as r) {
          @if (r.flagged) { <div class="notice">{{ r.notice }}</div> }
          @else { <p style="white-space:pre-wrap">{{ r.softened }}</p> }
        }
      </div>
      <p class="tiny muted center-text">AI 只是輔助，不會評斷你們的關係。</p>
    </div>
  `,
})
export class AiPage {
  private api = inject(Api);
  loc = inject(Location);

  bd = '';
  bdItems = signal<string[]>([]);
  sugItems = signal<string[]>([]);
  occ = '';
  occRes = signal('');
  sf = '';
  sfRes = signal<{ softened?: string; flagged: boolean; notice?: string } | null>(null);

  async breakdown() {
    if (!this.bd.trim()) return;
    this.bdItems.set((await this.api.post<{ items: string[] }>('/ai/todo-breakdown', { input: this.bd.trim() })).items);
  }
  async suggest() {
    this.sugItems.set((await this.api.post<{ items: string[] }>('/ai/date-suggestions', {})).items);
  }
  async message() {
    if (!this.occ.trim()) return;
    this.occRes.set((await this.api.post<{ text: string }>('/ai/anniversary-message', { occasion: this.occ.trim() })).text);
  }
  async soften() {
    if (!this.sf.trim()) return;
    this.sfRes.set(await this.api.post('/ai/soften', { text: this.sf.trim() }));
  }
}
