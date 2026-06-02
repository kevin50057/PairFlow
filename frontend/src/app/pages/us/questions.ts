import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Api } from '../../core/api';
import { DailyQuestion } from '../../core/models';

@Component({
  selector: 'pf-questions',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">每日問答</h1><span></span></div>
    <div class="screen stack">
      @if (q(); as d) {
        <div class="card card-warm">
          <div class="tag">{{ d.category }}</div>
          <h3 style="margin-top:8px">{{ d.questionText }}</h3>
          @if (!d.myAnswered) {
            <textarea class="textarea" placeholder="寫下你的答案…" name="a" [(ngModel)]="answer"></textarea>
            <button class="btn btn-primary btn-block" style="margin-top:8px" [disabled]="!answer.trim()" (click)="submit()">送出答案</button>
            <p class="tiny muted" style="margin-top:6px">雙方都回答後，才會解鎖彼此的答案 🔒</p>
          } @else {
            <div class="divider"></div>
            <div class="small muted">我的答案</div>
            <p>{{ d.myAnswer }}</p>
            @if (d.bothAnswered) {
              <div class="small muted">對方的答案</div>
              <p>{{ d.partnerAnswer }}</p>
            } @else {
              <p class="notice">已送出，等待對方回答後解鎖 💛</p>
            }
          }
        </div>
      }

      @if (answered().length) {
        <div class="section-title">回顧</div>
        @for (h of answered(); track h.id) {
          <div class="card">
            <b class="small">{{ h.questionText }}</b>
            <p class="small" style="margin-top:6px"><span class="muted">我：</span>{{ h.myAnswer }}</p>
            <p class="small"><span class="muted">對方：</span>{{ h.partnerAnswer }}</p>
          </div>
        }
      }
    </div>
  `,
})
export class QuestionsPage implements OnInit {
  private api = inject(Api);
  loc = inject(Location);

  q = signal<DailyQuestion | null>(null);
  history = signal<DailyQuestion[]>([]);
  answer = '';

  ngOnInit() { this.load(); }
  async load() {
    this.q.set(await this.api.get<DailyQuestion>('/questions/today'));
    this.history.set(await this.api.get<DailyQuestion[]>('/questions/history'));
  }
  answered() { return this.history().filter((h) => h.bothAnswered && h.id !== this.q()?.id); }

  async submit() {
    if (!this.answer.trim()) return;
    await this.api.post('/questions/today/answer', { answer: this.answer.trim() });
    this.answer = '';
    this.load();
  }
}
