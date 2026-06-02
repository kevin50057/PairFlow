import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';

@Component({
  selector: 'pf-settings',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">關係設定</h1><span></span></div>
    <div class="screen stack">
      <div class="card stack">
        <div class="section-title" style="margin-top:0">個人檔案</div>
        <div class="field"><label class="label">暱稱</label><input class="input" name="dn" [(ngModel)]="displayName" /></div>
        <button class="btn btn-ghost" (click)="saveProfile()">儲存</button>
        @if (saved()) { <span class="tiny" style="color:var(--ok)">已儲存 ✓</span> }
      </div>

      <div class="card stack">
        <div class="section-title" style="margin-top:0">在一起的開始日期</div>
        <input class="input" type="date" name="sd" [(ngModel)]="startDate" />
        <button class="btn btn-ghost" (click)="saveStartDate()">設定</button>
        @if (couple.couple()?.daysTogether != null) { <span class="tiny muted">已經在一起 {{ couple.couple()!.daysTogether }} 天 ❤️</span> }
      </div>

      <div class="card">
        <div class="section-title" style="margin-top:0">資料與隱私</div>
        <p class="small muted">你們的內容預設只有彼此看得到。解除綁定時可選擇封存或刪除共同資料。</p>
      </div>

      <button class="btn btn-outline btn-block" (click)="logout()">登出</button>

      <div class="card">
        <div class="section-title" style="margin-top:0">危險區</div>
        @if (!confirmBreakup()) {
          <button class="btn btn-outline btn-block" style="color:var(--danger)" (click)="confirmBreakup.set(true)">解除情侶綁定</button>
        } @else {
          <p class="small">確定要解除綁定嗎？共同空間會被封存。</p>
          <div class="row"><button class="btn btn-outline grow" (click)="confirmBreakup.set(false)">取消</button>
            <button class="btn" style="background:var(--danger);color:#fff" (click)="breakup()">確定解除</button></div>
        }
      </div>
    </div>
  `,
})
export class SettingsPage implements OnInit {
  private api = inject(Api);
  auth = inject(Auth);
  couple = inject(CoupleStore);
  loc = inject(Location);
  private router = inject(Router);

  displayName = '';
  startDate = '';
  saved = signal(false);
  confirmBreakup = signal(false);

  ngOnInit() {
    this.displayName = this.auth.user()?.displayName ?? '';
    this.startDate = this.couple.couple()?.relationshipStartDate ?? '';
  }

  async saveProfile() {
    await this.auth.updateProfile({ displayName: this.displayName.trim() });
    this.saved.set(true);
    setTimeout(() => this.saved.set(false), 1500);
  }

  async saveStartDate() {
    if (!this.startDate) return;
    await this.couple.setStartDate(this.startDate);
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  async breakup() {
    const c = this.couple.couple();
    if (!c) return;
    await this.api.post(`/couples/${c.id}/breakup`, { confirm: true, dataHandling: 'ARCHIVE' });
    this.couple.couple.set(null);
    this.router.navigateByUrl('/onboarding');
  }
}
