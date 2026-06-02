import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Api } from '../../core/api';
import { DatePlan } from '../../core/models';
import { DATE_TYPE, VOTE } from '../../core/labels';

@Component({
  selector: 'pf-dates',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="sel() ? sel.set(null) : loc.back()">‹</button>
      <h1 style="font-size:1.1rem">約會規劃</h1><span></span></div>

    <div class="screen stack">
      @if (sel(); as p) {
        <div class="card">
          <h3>{{ p.title }}</h3>
          <div class="row wrap"><span class="tag">{{ type(p.dateType) }}</span>@if (p.area) { <span class="tag">{{ p.area }}</span> }<span class="tag">{{ p.status }}</span></div>
        </div>
        <div class="section-title">候選方案</div>
        @for (c of p.candidates; track c.id) {
          <div class="card">
            <b>{{ c.title }}</b>@if (c.location) { <span class="tiny muted"> · {{ c.location }}</span> }
            <div class="row wrap" style="margin-top:8px">
              @for (v of votes; track v[0]) {
                <span class="chip" [class.active]="c.myVote === v[0]" (click)="vote(c.id, v[0])">{{ v[1] }}</span>
              }
            </div>
            <div class="tiny muted" style="margin-top:6px">投票：{{ tally(c.votes) }}</div>
            @if (p.status === 'PLANNING') {
              <button class="btn btn-ghost btn-sm" style="margin-top:8px" (click)="finalize(c.id)">就選這個 → 排進行事曆</button>
            }
            @if (p.chosenCandidateId === c.id) { <span class="badge" style="margin-top:8px;display:inline-block">已選定 ✓</span> }
          </div>
        }
        @if (p.status === 'PLANNING') {
          <div class="card stack">
            <input class="input" placeholder="新增候選方案…" name="ct" [(ngModel)]="cf.title" />
            <input class="input" placeholder="地點（選填）" name="cl" [(ngModel)]="cf.location" />
            <input class="input" type="datetime-local" name="fs" [(ngModel)]="finalizeAt" title="選定後的開始時間" />
            <button class="btn btn-outline btn-block" (click)="addCandidate()">加入候選</button>
            <p class="tiny muted">小提醒：選定方案前先填上方「開始時間」，系統會自動建立行程與準備任務。</p>
          </div>
        }
      } @else {
        @if (show()) {
          <div class="card stack">
            <input class="input" placeholder="這次約會的主題…" name="t" [(ngModel)]="f.title" />
            <div class="grid2">
              <select class="input" name="dt" [(ngModel)]="f.dateType">
                @for (d of types; track d[0]) { <option [value]="d[0]">{{ d[1] }}</option> }
              </select>
              <input class="input" placeholder="地區" name="ar" [(ngModel)]="f.area" />
            </div>
            <button class="btn btn-primary btn-block" [disabled]="!f.title.trim()" (click)="create()">建立規劃</button>
          </div>
        } @else {
          <button class="btn btn-primary btn-block" (click)="show.set(true)">＋ 開一個新的約會規劃</button>
        }
        @for (p of plans(); track p.id) {
          <div class="card" (click)="open(p)" style="cursor:pointer">
            <div class="between"><b>{{ p.title }}</b><span class="tag">{{ p.status }}</span></div>
            <div class="tiny muted">{{ type(p.dateType) }} · {{ p.candidates.length }} 個候選</div>
          </div>
        }
      }
    </div>
  `,
})
export class DatesPage implements OnInit {
  private api = inject(Api);
  loc = inject(Location);

  plans = signal<DatePlan[]>([]);
  sel = signal<DatePlan | null>(null);
  show = signal(false);
  types = Object.entries(DATE_TYPE);
  votes = Object.entries(VOTE);
  f = { title: '', dateType: 'FOOD', area: '' };
  cf = { title: '', location: '' };
  finalizeAt = '';

  ngOnInit() { this.load(); }
  async load() { this.plans.set(await this.api.get<DatePlan[]>('/date-plans')); }

  async create() {
    if (!this.f.title.trim()) return;
    const p = await this.api.post<DatePlan>('/date-plans', { title: this.f.title.trim(), dateType: this.f.dateType, area: this.f.area || null });
    this.f = { title: '', dateType: 'FOOD', area: '' };
    this.show.set(false);
    await this.load();
    this.open(p);
  }

  async open(p: DatePlan) { this.sel.set(await this.api.get<DatePlan>('/date-plans/' + p.id)); }
  async refresh() { const s = this.sel(); if (s) this.sel.set(await this.api.get<DatePlan>('/date-plans/' + s.id)); }

  async addCandidate() {
    const s = this.sel();
    if (!s || !this.cf.title.trim()) return;
    await this.api.post(`/date-plans/${s.id}/candidates`, { title: this.cf.title.trim(), location: this.cf.location || null });
    this.cf = { title: '', location: '' };
    this.refresh();
  }

  async vote(candidateId: string, v: string) {
    const s = this.sel();
    if (!s) return;
    await this.api.post(`/date-plans/${s.id}/candidates/${candidateId}/vote`, { vote: v });
    this.refresh();
  }

  async finalize(candidateId: string) {
    const s = this.sel();
    if (!s) return;
    const start = this.finalizeAt ? new Date(this.finalizeAt).toISOString() : new Date(Date.now() + 86400000).toISOString();
    await this.api.post(`/date-plans/${s.id}/finalize`, { candidateId, startTime: start, createTodos: true });
    this.refresh();
  }

  type(c: string) { return DATE_TYPE[c] ?? c; }
  tally(vs: { vote: string }[]) {
    if (!vs.length) return '尚無';
    return vs.map((v) => VOTE[v.vote] ?? v.vote).join('、');
  }
}
