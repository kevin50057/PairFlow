import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Api } from '../../core/api';
import { CoupleStore } from '../../core/couple';
import { TodayMood } from '../../core/models';
import { MOOD, REACTION } from '../../core/labels';

@Component({
  selector: 'pf-mood',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">心情</h1><span></span></div>
    <div class="screen stack">
      <div class="card">
        <div class="section-title" style="margin-top:0">今天的你？</div>
        <div class="row wrap">
          @for (m of moods; track m[0]) {
            <span class="chip" [class.active]="picked === m[0]" (click)="picked = m[0]">{{ m[1].emoji }} {{ m[1].label }}</span>
          }
        </div>
        <textarea class="textarea" style="margin-top:10px;min-height:60px" placeholder="想說一句話…（選填）" name="note" [(ngModel)]="note"></textarea>
        <label class="row small" style="margin-top:8px;cursor:pointer">
          <input type="checkbox" name="nr" [(ngModel)]="needResponse" /> 希望對方回應我
        </label>
        <button class="btn btn-primary btn-block" style="margin-top:10px" [disabled]="!picked" (click)="post()">打卡心情</button>
      </div>

      @if (today(); as t) {
        @if (t.partner; as p) {
          <div class="card card-warm">
            <div class="section-title" style="margin-top:0">{{ couple.couple()?.partner?.displayName }} 今天</div>
            <div class="row"><span style="font-size:1.8rem">{{ emoji(p.mood) }}</span>
              <div class="grow"><b>{{ label(p.mood) }}</b>@if (p.note) { <div class="muted small">{{ p.note }}</div> }</div>
            </div>
            <div class="row wrap" style="margin-top:10px">
              @for (r of reactions; track r[0]) { <button class="chip" (click)="react(r[0])">{{ r[1] }}</button> }
            </div>
            @if (p.reactions.length) {
              <div class="tiny muted" style="margin-top:8px">你回應了：{{ reactionList(p.reactions) }}</div>
            }
          </div>
        }
        @if (t.me; as me) {
          <div class="card">
            <div class="section-title" style="margin-top:0">我的今日</div>
            <div class="row"><span style="font-size:1.8rem">{{ emoji(me.mood) }}</span><b>{{ label(me.mood) }}</b></div>
            @if (me.reactions.length) { <div class="tiny muted" style="margin-top:6px">對方回應了你 {{ me.reactions.length }} 次 ❤️</div> }
          </div>
        }
      }
      <p class="tiny muted center-text">心情分享是為了靠近彼此，沒有壓力，想說再說 🌿</p>
    </div>
  `,
})
export class MoodPage implements OnInit {
  private api = inject(Api);
  loc = inject(Location);
  couple = inject(CoupleStore);

  today = signal<TodayMood | null>(null);
  moods = Object.entries(MOOD);
  reactions = Object.entries(REACTION);
  picked = '';
  note = '';
  needResponse = false;

  ngOnInit() { this.load(); }
  async load() { this.today.set(await this.api.get<TodayMood>('/moods/today')); }

  async post() {
    if (!this.picked) return;
    await this.api.post('/moods', { mood: this.picked, emoji: MOOD[this.picked].emoji, note: this.note || null, needResponse: this.needResponse });
    this.picked = ''; this.note = ''; this.needResponse = false;
    this.load();
  }

  async react(type: string) {
    const p = this.today()?.partner;
    if (!p) return;
    await this.api.post(`/moods/${p.id}/reactions`, { reaction: type });
    this.load();
  }

  label(c: string) { return MOOD[c]?.label ?? c; }
  emoji(c: string) { return MOOD[c]?.emoji ?? '🙂'; }
  reactionList(rs: { reaction: string }[]) { return rs.map((r) => REACTION[r.reaction] ?? r.reaction).join('、'); }
}
