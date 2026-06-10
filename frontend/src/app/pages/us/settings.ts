import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';

type BreakupPhase = 'idle' | 'confirming' | 'pending' | 'partner_pending';

@Component({
  selector: 'pf-settings',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">關係設定</h1><span></span></div>
    <div class="screen stack">
      <a class="card between" routerLink="/me" style="align-items:center">
        <span><b>個人檔案</b><div class="tiny muted">暱稱 · 生日 · 性別 · 大頭貼</div></span>
        <span style="color:var(--muted);font-size:1.3rem">›</span>
      </a>

      <div class="card stack">
        <div class="section-title" style="margin-top:0">在一起的開始日期</div>
        <input class="input" type="date" name="sd" [(ngModel)]="startDate" />
        <button class="btn btn-ghost" (click)="saveStartDate()">設定</button>
        @if (couple.couple()?.daysTogether != null) { <span class="tiny muted">已經在一起 {{ couple.couple()!.daysTogether }} 天 ❤️</span> }
      </div>

      <div class="card stack">
        <div class="section-title" style="margin-top:0">資料與隱私</div>
        <p class="small muted">你們的內容預設只有彼此看得到。解除綁定後可以匯出個人資料。</p>
        <button class="btn btn-ghost" (click)="exportData()">匯出我的資料</button>
        @if (exportMsg()) { <span class="tiny muted">{{ exportMsg() }}</span> }
      </div>

      <button class="btn btn-outline btn-block" (click)="doLogout()">登出</button>

      <div class="card stack">
        <div class="section-title" style="margin-top:0">危險區</div>

        @switch (breakupPhase()) {
          @case ('idle') {
            <button class="btn btn-outline btn-block" style="color:var(--danger)" (click)="breakupPhase.set('confirming')">解除情侶綁定</button>
          }
          @case ('confirming') {
            <p class="small">解除綁定需要<strong>雙方確認</strong>。發送請求後，對方需要在 7 天內確認。</p>
            <div class="row">
              <button class="btn btn-outline grow" (click)="breakupPhase.set('idle')">取消</button>
              <button class="btn grow" style="background:var(--danger);color:#fff" (click)="initiateBreakup()">發送解除請求</button>
            </div>
          }
          @case ('pending') {
            <p class="small" style="color:var(--danger)">解除綁定請求已發出，等待對方確認（7 天內有效）。</p>
            <button class="btn btn-outline btn-block" (click)="cancelBreakup()">取消解除請求</button>
          }
          @case ('partner_pending') {
            <p class="small" style="color:var(--danger)">你的伴侶已請求解除綁定。你確認後關係將結束。</p>
            <div class="row">
              <button class="btn btn-outline grow" (click)="breakupPhase.set('idle')">暫不確認</button>
              <button class="btn grow" style="background:var(--danger);color:#fff" (click)="confirmBreakup()">我確認解除</button>
            </div>
          }
        }
        @if (breakupError()) { <span class="tiny" style="color:var(--danger)">{{ breakupError() }}</span> }
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
  exportMsg = signal('');
  breakupPhase = signal<BreakupPhase>('idle');
  breakupError = signal('');

  ngOnInit() {
    this.displayName = this.auth.user()?.displayName ?? '';
    this.startDate = this.couple.couple()?.relationshipStartDate ?? '';
    this.checkBreakupStatus();
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

  async doLogout() {
    await this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  async exportData() {
    const c = this.couple.couple();
    if (!c) return;
    try {
      const data = await this.api.get(`/couples/${c.id}/export`);
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'pairflow-export.json'; a.click();
      URL.revokeObjectURL(url);
      this.exportMsg.set('匯出成功！');
    } catch {
      this.exportMsg.set('匯出失敗，請稍後再試。');
    }
    setTimeout(() => this.exportMsg.set(''), 3000);
  }

  async initiateBreakup() {
    const c = this.couple.couple();
    if (!c) return;
    try {
      await this.api.post(`/couples/${c.id}/breakup`, { dataHandling: 'ARCHIVE' });
      this.breakupPhase.set('pending');
      this.breakupError.set('');
    } catch (e: any) {
      this.breakupError.set(e?.error?.message ?? '操作失敗');
    }
  }

  async cancelBreakup() {
    const c = this.couple.couple();
    if (!c) return;
    try {
      await this.api.del(`/couples/${c.id}/breakup`);
      this.breakupPhase.set('idle');
      this.breakupError.set('');
    } catch (e: any) {
      this.breakupError.set(e?.error?.message ?? '取消失敗');
    }
  }

  async confirmBreakup() {
    const c = this.couple.couple();
    if (!c) return;
    try {
      await this.api.post(`/couples/${c.id}/breakup/confirm`);
      this.couple.couple.set(null);
      this.router.navigateByUrl('/onboarding');
    } catch (e: any) {
      this.breakupError.set(e?.error?.message ?? '確認失敗');
    }
  }

  private async checkBreakupStatus() {
    const c = this.couple.couple();
    if (!c) return;
    try {
      const status = await this.api.get<any>(`/couples/${c.id}/breakup/status`);
      if (!status) return;
      const myId = this.auth.user()?.id;
      this.breakupPhase.set(status.initiatorId === myId ? 'pending' : 'partner_pending');
    } catch { /* no pending breakup */ }
  }
}
