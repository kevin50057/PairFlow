import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, Location } from '@angular/common';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { Expense, ExpenseSummary } from '../../core/models';

@Component({
  selector: 'pf-finance',
  imports: [FormsModule, DecimalPipe],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">輕量記帳</h1>
      <button class="btn btn-ghost btn-sm" (click)="show.set(!show())">＋ 記一筆</button>
    </div>
    <div class="screen stack">
      @if (summary(); as s) {
        <div class="card card-warm center-text">
          <div class="hero-day">{{ s.total | number }}</div>
          <div class="muted small">共 {{ s.count }} 筆 · 本月共同支出</div>
        </div>
      }

      @if (show()) {
        <div class="card stack">
          <input class="input" type="number" placeholder="金額" name="amt" [(ngModel)]="f.amount" />
          <div class="grid2">
            <select class="input" name="cat" [(ngModel)]="f.category">
              <option value="DATE">約會</option><option value="FOOD">餐飲</option><option value="TRAVEL">旅行</option>
              <option value="LIVING">生活</option><option value="GIFT">禮物</option><option value="OTHER">其他</option>
            </select>
            <select class="input" name="payer" [(ngModel)]="f.paidBy">
              <option value="ME">我付</option><option value="PARTNER">對方付</option>
            </select>
          </div>
          <select class="input" name="split" [(ngModel)]="f.splitType">
            <option value="NONE">只記錄</option><option value="EQUAL">平分</option><option value="I_PAID">我請客</option>
            <option value="PARTNER_PAID">對方請客</option><option value="CUSTOM">自訂</option>
          </select>
          <input class="input" placeholder="備註（選填）" name="note" [(ngModel)]="f.note" />
          <button class="btn btn-primary btn-block" [disabled]="!f.amount" (click)="create()">記帳</button>
        </div>
      }

      @if (!expenses().length) { <div class="empty">還沒有記錄，輕鬆記一筆吧 💰</div> }
      @for (e of expenses(); track e.id) {
        <div class="card">
          <div class="between">
            <div><b>{{ e.amount | number }}</b> <span class="tag">{{ catLabel(e.category) }}</span>
              @if (e.note) { <span class="small muted"> · {{ e.note }}</span> }
            </div>
            <div class="tiny muted center-text">{{ payer(e) }}<br />{{ date(e.spentAt) }}</div>
          </div>
        </div>
      }
      <p class="tiny muted center-text">記帳只是為了清楚，不是為了算誰欠誰 🌿</p>
    </div>
  `,
})
export class FinancePage implements OnInit {
  private api = inject(Api);
  private auth = inject(Auth);
  loc = inject(Location);

  expenses = signal<Expense[]>([]);
  summary = signal<ExpenseSummary | null>(null);
  show = signal(false);
  f = { amount: null as number | null, category: 'DATE', paidBy: 'ME', splitType: 'EQUAL', note: '' };

  ngOnInit() { this.load(); }
  async load() {
    this.expenses.set(await this.api.get<Expense[]>('/expenses'));
    this.summary.set(await this.api.get<ExpenseSummary>('/expenses/summary'));
  }

  async create() {
    if (!this.f.amount) return;
    await this.api.post('/expenses', {
      amount: this.f.amount, category: this.f.category, paidBy: this.f.paidBy,
      splitType: this.f.splitType, note: this.f.note || null,
    });
    this.f = { amount: null, category: 'DATE', paidBy: 'ME', splitType: 'EQUAL', note: '' };
    this.show.set(false);
    this.load();
  }

  payer(e: Expense) { return e.paidByUserId === this.auth.user()?.id ? '我付' : '對方付'; }
  catLabel(c: string) {
    const m: Record<string, string> = { DATE: '約會', FOOD: '餐飲', TRAVEL: '旅行', LIVING: '生活', GIFT: '禮物', OTHER: '其他' };
    return m[c] ?? c;
  }
  date(iso: string) { return new Date(iso).toLocaleDateString('zh-TW', { month: 'numeric', day: 'numeric' }); }
}
