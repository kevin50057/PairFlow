import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../core/auth';

@Component({
  selector: 'pf-login',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="center">
      <div class="card card-warm" style="width:100%;max-width:380px">
        <div class="center-text" style="margin-bottom:18px">
          <div class="hero-day">PairFlow</div>
          <p class="muted small">一起生活、一起記錄、一起完成事情 ❤️</p>
        </div>
        <form (ngSubmit)="submit()" class="stack">
          <div class="field">
            <label class="label">Email</label>
            <input class="input" name="email" type="email" [(ngModel)]="email" required autocomplete="email" />
          </div>
          <div class="field">
            <label class="label">密碼</label>
            <input class="input" name="password" type="password" [(ngModel)]="password" required autocomplete="current-password" />
          </div>
          @if (error()) { <div class="error">{{ error() }}</div> }
          <button class="btn btn-primary btn-block" type="submit" [disabled]="loading()">
            {{ loading() ? '登入中…' : '登入' }}
          </button>
        </form>
        <p class="center-text small muted" style="margin-top:14px">
          還沒有帳號？ <a class="link" routerLink="/register">註冊</a>
        </p>
      </div>
    </div>
  `,
})
export class LoginPage {
  private auth = inject(Auth);
  private router = inject(Router);

  email = '';
  password = '';
  error = signal('');
  loading = signal(false);

  async submit() {
    this.error.set('');
    this.loading.set(true);
    try {
      await this.auth.login(this.email.trim().toLowerCase(), this.password);
      this.router.navigateByUrl('/home');
    } catch (e: any) {
      this.error.set(e?.error?.message ?? '登入失敗，請檢查帳號或密碼');
    } finally {
      this.loading.set(false);
    }
  }
}
