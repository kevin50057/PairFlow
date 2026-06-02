import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../core/api';
import { DailyQuestion } from '../core/models';

/**
 * Daily question popup. Shown once a day when the app opens, if the current user
 * hasn't answered today's question yet. Both partners answer; once both have,
 * the modal reveals each other's answers (spec 7.9).
 */
@Component({
  selector: 'pf-daily-question',
  imports: [FormsModule],
  template: `
    @if (open() && q(); as d) {
      <div class="dq-backdrop" (click)="dismiss()">
        <div class="dq-card" (click)="$event.stopPropagation()">
          <button class="dq-close" (click)="dismiss()" aria-label="關閉">✕</button>
          <div class="dq-eyebrow">✨ 今天的問題</div>
          <h2 class="dq-q">{{ d.questionText }}</h2>

          @if (!d.myAnswered) {
            <textarea class="textarea dq-input" name="a" [(ngModel)]="answer" placeholder="寫下你的答案…"></textarea>
            <button class="btn btn-primary btn-block" [disabled]="!answer.trim() || saving()" (click)="submit()">
              {{ saving() ? '送出中…' : '送出答案' }}
            </button>
            <p class="dq-hint">🔒 雙方都回答後，才會解鎖彼此的答案</p>
          } @else if (d.bothAnswered) {
            <div class="dq-reveal">
              <div class="dq-ans"><span class="dq-who">我</span>{{ d.myAnswer }}</div>
              <div class="dq-ans partner"><span class="dq-who">對方</span>{{ d.partnerAnswer }}</div>
            </div>
            <button class="btn btn-primary btn-block" (click)="dismiss()">完成 ❤️</button>
          } @else {
            <div class="dq-wait">✅ 已送出你的答案<div class="muted small" style="margin-top:4px">等對方回答後，就會一起解鎖 💛</div></div>
            <button class="btn btn-ghost btn-block" (click)="dismiss()">好</button>
          }
        </div>
      </div>
    }
  `,
})
export class DailyQuestionModal implements OnInit {
  private api = inject(Api);

  q = signal<DailyQuestion | null>(null);
  open = signal(false);
  saving = signal(false);
  answer = '';

  async ngOnInit() {
    try {
      const d = await this.api.get<DailyQuestion>('/questions/today');
      this.q.set(d);
      if (!d.myAnswered && localStorage.getItem(this.key(d)) == null) {
        this.open.set(true);
      }
    } catch { /* not paired / offline — silently skip */ }
  }

  async submit() {
    if (!this.answer.trim()) return;
    this.saving.set(true);
    try {
      this.q.set(await this.api.post<DailyQuestion>('/questions/today/answer', { answer: this.answer.trim() }));
      this.answer = '';
    } finally {
      this.saving.set(false);
    }
  }

  dismiss() {
    const d = this.q();
    if (d) localStorage.setItem(this.key(d), '1'); // don't reopen today
    this.open.set(false);
  }

  private key(d: DailyQuestion) {
    return 'pf_dq_' + d.date;
  }
}
