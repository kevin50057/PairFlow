import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../core/auth';

@Component({
  selector: 'pf-register',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="center">
      <div class="card card-warm" style="width:100%;max-width:380px">
        <div class="center-text" style="margin-bottom:18px">
          <div class="hero-day">建立帳號</div>
          <p class="muted small">開始你們的共同空間</p>
        </div>
        <form (ngSubmit)="submit()" class="stack">
          <div class="field">
            <label class="label">暱稱</label>
            <input class="input" name="displayName" [(ngModel)]="displayName" required />
          </div>
          <div class="field">
            <label class="label">Email</label>
            <input class="input" name="email" type="email" [(ngModel)]="email" required autocomplete="email" />
          </div>
          <div class="field">
            <label class="label">密碼（至少 6 碼）</label>
            <input class="input" name="password" type="password" [(ngModel)]="password" required autocomplete="new-password" />
          </div>
          @if (error()) { <div class="error">{{ error() }}</div> }
          <button class="btn btn-primary btn-block" type="submit" [disabled]="loading()">
            {{ loading() ? '建立中…' : '註冊' }}
          </button>
        </form>
        <p class="center-text small muted" style="margin-top:14px">
          已經有帳號？ <a class="link" routerLink="/login">登入</a>
        </p>
      </div>
    </div>
  `,
})
export class RegisterPage {
  private auth = inject(Auth);
  private router = inject(Router);

  displayName = '';
  email = '';
  password = '';
  error = signal('');
  loading = signal(false);

  async submit() {
    this.error.set('');
    this.loading.set(true);
    try {
      await this.auth.register(this.email.trim().toLowerCase(), this.password, this.displayName.trim());
      this.router.navigateByUrl('/onboarding');
    } catch (e: any) {
      const details = e?.error?.details?.length ? e.error.details.join('、') : null;
      this.error.set(details ?? e?.error?.message ?? '註冊失敗');
    } finally {
      this.loading.set(false);
    }
  }
}
