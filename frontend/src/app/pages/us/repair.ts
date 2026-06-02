import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { Repair } from '../../core/models';
import { REPAIR_STATE, RESPONSE_TYPE } from '../../core/labels';

@Component({
  selector: 'pf-repair',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">吵架修復</h1>
      <button class="btn btn-ghost btn-sm" (click)="show.set(!show())">開始</button>
    </div>
    <div class="screen stack">
      <p class="tiny muted">這不是評判對錯，只是幫你把想說的話，用溫和一點的方式表達出來。</p>

      @if (show()) {
        <div class="card stack">
          <div class="label">現在的你？</div>
          <div class="row wrap">
            @for (s of states; track s[0]) {
              <span class="chip" [class.active]="f.state === s[0]" (click)="f.state = s[0]">{{ s[1] }}</span>
            }
          </div>
          <textarea class="textarea" placeholder="寫下你的感受…" name="fe" [(ngModel)]="f.feelings"></textarea>
          <input class="input" placeholder="最想讓對方知道的重點（選填）" name="kp" [(ngModel)]="f.keyPoints" />
          <button class="btn btn-primary btn-block" [disabled]="!f.feelings.trim()" (click)="create()">整理成溫和的表達</button>
        </div>
      }

      @for (r of sessions(); track r.id) {
        <div class="card">
          <div class="between">
            <b class="small">{{ mine(r) ? '我發起' : '對方發起' }} · {{ state(r.state) }}</b>
            <span class="tag">{{ r.status }}</span>
          </div>
          @if (r.flagged && r.notice) { <div class="notice" style="margin-top:8px">{{ r.notice }}</div> }
          @else if (r.softenedMessage) { <p style="margin-top:8px;white-space:pre-wrap">{{ r.softenedMessage }}</p> }

          @if (mine(r) && r.status === 'DRAFT' && !r.flagged) {
            <button class="btn btn-primary btn-sm" style="margin-top:8px" (click)="send(r)">送給對方</button>
          }
          @if (!mine(r) && r.status === 'SENT') {
            <div class="row wrap" style="margin-top:8px">
              @for (rt of responses; track rt[0]) { <span class="chip" (click)="respond(r, rt[0])">{{ rt[1] }}</span> }
            </div>
          }
          @if (r.responseType) {
            <div class="tiny" style="margin-top:8px;color:var(--primary-ink)">回應：{{ resp(r.responseType) }}@if (r.responseNote) { — {{ r.responseNote }} }</div>
          }
        </div>
      }
    </div>
  `,
})
export class RepairPage implements OnInit {
  private api = inject(Api);
  private auth = inject(Auth);
  loc = inject(Location);

  sessions = signal<Repair[]>([]);
  show = signal(false);
  states = Object.entries(REPAIR_STATE);
  responses = Object.entries(RESPONSE_TYPE);
  f = { state: 'WANT_UNDERSTOOD', feelings: '', keyPoints: '' };

  ngOnInit() { this.load(); }
  async load() { this.sessions.set(await this.api.get<Repair[]>('/repair')); }

  async create() {
    if (!this.f.feelings.trim()) return;
    await this.api.post('/repair', { state: this.f.state, feelings: this.f.feelings.trim(), keyPoints: this.f.keyPoints || null });
    this.f = { state: 'WANT_UNDERSTOOD', feelings: '', keyPoints: '' };
    this.show.set(false);
    this.load();
  }

  async send(r: Repair) { await this.api.post(`/repair/${r.id}/send`); this.load(); }
  async respond(r: Repair, type: string) { await this.api.post(`/repair/${r.id}/respond`, { responseType: type }); this.load(); }

  mine(r: Repair) { return r.initiatorId === this.auth.user()?.id; }
  state(c: string) { return REPAIR_STATE[c] ?? c; }
  resp(c: string) { return RESPONSE_TYPE[c] ?? c; }
}
