import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideCake, LucideChevronRight, LucideLogOut, LucideSettings } from '@lucide/angular';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';
import { Gender } from '../../core/models';
import { Avatar } from '../../shared/avatar';

@Component({
  selector: 'pf-profile',
  imports: [FormsModule, RouterLink, Avatar, LucideCake, LucideChevronRight, LucideLogOut, LucideSettings],
  template: `
    <div class="appbar">
      <div>
        <h1>我的檔案</h1>
        <div class="subtitle">這些資料另一半也看得到</div>
      </div>
    </div>

    <div class="screen stack">
      <!-- hero -->
      <div class="card card-warm profile-hero">
        <pf-avatar [src]="form.avatarUrl" [fallback]="myFallback()" [size]="92" />
        <div class="grow">
          <h2 style="margin:0 0 2px">{{ form.displayName || auth.user()?.displayName }}</h2>
          <div class="muted small">{{ auth.user()?.email }}</div>
          @if (form.birthday) {
            <div class="hero-meta"><svg lucideCake size="14"></svg> {{ birthdayLabel(form.birthday) }}（還有 {{ daysToBirthday(form.birthday) }} 天）</div>
          }
        </div>
      </div>

      <!-- avatar picker -->
      <div class="card stack">
        <div class="section-title" style="margin-top:0">大頭貼</div>
        <div class="avatar-picker">
          @for (opt of avatarOptions; track opt) {
            <button class="pick" [class.sel]="form.avatarUrl === opt" (click)="form.avatarUrl = opt" type="button">
              <pf-avatar [src]="opt" [size]="46" />
            </button>
          }
        </div>
      </div>

      <!-- editable fields -->
      <div class="card stack">
        <div class="field">
          <label class="label">暱稱</label>
          <input class="input" name="dn" [(ngModel)]="form.displayName" maxlength="50" />
        </div>

        <div class="field">
          <label class="label">生日</label>
          <input class="input" type="date" name="bd" [(ngModel)]="form.birthday" />
          <div class="tiny muted" style="margin-top:4px">設定後生日會出現在首頁倒數，並在前 7 天、前 1 天與當天提醒對方 🎂</div>
        </div>

        <div class="field">
          <label class="label">性別</label>
          <div class="seg">
            @for (g of genders; track g[0]) {
              <button class="seg-btn" type="button" [class.sel]="form.gender === g[0]" (click)="form.gender = g[0]">{{ g[1] }}</button>
            }
          </div>
        </div>

        <div class="field">
          <label class="label">一句話介紹</label>
          <input class="input" name="bio" [(ngModel)]="form.bio" maxlength="200" placeholder="讓對方更認識你…" />
        </div>

        <button class="btn btn-primary btn-block" (click)="save()" [disabled]="saving()">{{ saving() ? '儲存中…' : '儲存' }}</button>
        @if (saved()) { <span class="tiny center-text" style="color:var(--ok)">已儲存 ✓</span> }
        @if (error()) { <span class="tiny center-text" style="color:var(--danger)">{{ error() }}</span> }
      </div>

      <!-- partner snapshot -->
      @if (couple.couple()?.partner; as p) {
        <div class="card stack">
          <div class="section-title" style="margin-top:0">另一半</div>
          <div class="row">
            <pf-avatar [src]="p.avatarUrl" [fallback]="p.gender === 'MALE' ? 'boy' : 'girl'" [size]="52" />
            <div class="grow">
              <b>{{ p.displayName }}</b>
              @if (p.bio) { <div class="muted small">{{ p.bio }}</div> }
              @if (p.birthday) {
                <div class="tiny" style="color:var(--primary-ink);margin-top:2px"><svg lucideCake size="13"></svg> {{ birthdayLabel(p.birthday) }} · 還有 {{ daysToBirthday(p.birthday) }} 天</div>
              } @else {
                <div class="tiny muted">還沒設定生日</div>
              }
            </div>
          </div>
        </div>
      }

      <a class="card between" routerLink="/us/settings" style="align-items:center">
        <span class="row" style="gap:8px"><svg lucideSettings size="18"></svg> 關係設定</span>
        <svg lucideChevronRight size="18" color="var(--muted)"></svg>
      </a>

      <button class="btn btn-outline btn-block" (click)="logout()"><svg lucideLogOut size="18"></svg> 登出</button>
    </div>
  `,
})
export class ProfilePage implements OnInit {
  auth = inject(Auth);
  couple = inject(CoupleStore);
  private router = inject(Router);

  genders: [Gender, string][] = [['MALE', '他 / 男生'], ['FEMALE', '她 / 女生'], ['OTHER', '其他']];
  avatarOptions = [
    '/avatars/boy.png', '/avatars/girl.png',
    'emoji:🐻', 'emoji:🐰', 'emoji:🐱', 'emoji:🐶',
    'emoji:🦊', 'emoji:🐼', 'emoji:🐧', 'emoji:🐯',
  ];

  form: { displayName: string; avatarUrl?: string; birthday: string; gender?: Gender; bio: string } = {
    displayName: '', avatarUrl: undefined, birthday: '', gender: undefined, bio: '',
  };
  saving = signal(false);
  saved = signal(false);
  error = signal('');

  async ngOnInit() {
    if (!this.auth.user()) { try { await this.auth.loadMe(); } catch { /* guard handles */ } }
    if (!this.couple.couple()) { try { await this.couple.load(); } catch { /* ignore */ } }
    const u = this.auth.user();
    if (u) {
      this.form = {
        displayName: u.displayName ?? '',
        avatarUrl: u.avatarUrl,
        birthday: u.birthday ?? '',
        gender: u.gender,
        bio: u.bio ?? '',
      };
    }
  }

  myFallback(): 'boy' | 'girl' { return this.form.gender === 'FEMALE' ? 'girl' : 'boy'; }

  async save() {
    this.error.set('');
    this.saving.set(true);
    try {
      const patch: Record<string, unknown> = {
        displayName: this.form.displayName.trim(),
        avatarUrl: this.form.avatarUrl ?? '',
        bio: this.form.bio ?? '',
      };
      if (this.form.gender) patch['gender'] = this.form.gender;
      if (this.form.birthday) patch['birthday'] = this.form.birthday;
      await this.auth.updateProfile(patch);
      this.saved.set(true);
      setTimeout(() => this.saved.set(false), 1800);
    } catch (e: any) {
      this.error.set(e?.error?.message ?? '儲存失敗，請稍後再試');
    } finally {
      this.saving.set(false);
    }
  }

  async logout() {
    await this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  private nextBirthday(d: string): Date {
    const [, m, day] = d.split('-').map(Number);
    const today = new Date(); today.setHours(0, 0, 0, 0);
    let next = new Date(today.getFullYear(), m - 1, day);
    if (next < today) next = new Date(today.getFullYear() + 1, m - 1, day);
    return next;
  }
  daysToBirthday(d: string): number {
    const today = new Date(); today.setHours(0, 0, 0, 0);
    return Math.round((this.nextBirthday(d).getTime() - today.getTime()) / 86400000);
  }
  birthdayLabel(d: string): string {
    const [, m, day] = d.split('-').map(Number);
    return `${m} 月 ${day} 日`;
  }
}
