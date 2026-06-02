import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';

@Component({
  selector: 'pf-onboarding',
  imports: [FormsModule],
  template: `
    <div class="center">
      <div class="card" style="width:100%;max-width:400px">
        <div class="center-text" style="margin-bottom:14px">
          <div class="hero-day">配對你們的空間</div>
          <p class="muted small">一個帳號只能和一位伴侶配對</p>
        </div>

        <div class="chip-row" style="justify-content:center;margin-bottom:14px">
          <span class="chip" [class.active]="mode() === 'create'" (click)="mode.set('create')">建立邀請碼</span>
          <span class="chip" [class.active]="mode() === 'join'" (click)="mode.set('join')">輸入邀請碼</span>
        </div>

        @if (mode() === 'create') {
          <div class="stack center-text">
            @if (!code()) {
              <p class="muted small">產生一組邀請碼，傳給另一半，等他/她輸入即可配對。</p>
              <button class="btn btn-primary btn-block" (click)="invite()" [disabled]="loading()">
                {{ loading() ? '產生中…' : '產生邀請碼' }}
              </button>
            } @else {
              <p class="muted small">把這組邀請碼傳給另一半：</p>
              <div class="hero-day" style="letter-spacing:.15em">{{ code() }}</div>
              <p class="tiny muted">對方輸入後，回到這裡會自動進入你們的空間。</p>
              <button class="btn btn-ghost btn-block" (click)="refresh()">我已配對，進入</button>
            }
          </div>
        } @else {
          <form (ngSubmit)="join()" class="stack">
            <div class="field">
              <label class="label">伴侶的邀請碼</label>
              <input class="input" name="code" [(ngModel)]="joinCode" style="letter-spacing:.12em;text-align:center" />
            </div>
            <button class="btn btn-primary btn-block" type="submit" [disabled]="loading()">
              {{ loading() ? '配對中…' : '配對' }}
            </button>
          </form>
        }

        @if (error()) { <div class="error center-text" style="margin-top:10px">{{ error() }}</div> }
        <p class="center-text small muted" style="margin-top:16px">
          <a class="link" (click)="logout()">登出</a>
        </p>
      </div>
    </div>
  `,
})
export class OnboardingPage {
  private store = inject(CoupleStore);
  private auth = inject(Auth);
  private router = inject(Router);

  mode = signal<'create' | 'join'>('create');
  code = signal('');
  joinCode = '';
  error = signal('');
  loading = signal(false);

  async invite() {
    this.run(async () => {
      const res = await this.store.createInvite();
      this.code.set(res.code);
    });
  }

  async join() {
    if (!this.joinCode.trim()) return;
    this.run(async () => {
      await this.store.join(this.joinCode.trim());
      this.router.navigateByUrl('/home');
    });
  }

  async refresh() {
    this.run(async () => {
      const couple = await this.store.load();
      if (couple) this.router.navigateByUrl('/home');
      else this.error.set('還沒配對成功，請確認對方已輸入邀請碼。');
    });
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  private async run(fn: () => Promise<void>) {
    this.error.set('');
    this.loading.set(true);
    try {
      await fn();
    } catch (e: any) {
      this.error.set(e?.error?.message ?? '操作失敗');
    } finally {
      this.loading.set(false);
    }
  }
}
