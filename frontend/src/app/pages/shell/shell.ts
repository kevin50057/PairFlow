import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';
import { DailyQuestionModal } from '../../shared/daily-question-modal';

@Component({
  selector: 'pf-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, DailyQuestionModal],
  template: `
    <div class="bg-hearts"><span>💗</span><span>🩷</span><span>💕</span></div>
    <router-outlet />
    <pf-daily-question />
    <nav class="bottomnav">
      <a routerLink="/home" routerLinkActive="active"><span class="ic">🏠</span>首頁</a>
      <a routerLink="/todos" routerLinkActive="active"><span class="ic">📝</span>任務</a>
      <a routerLink="/calendar" routerLinkActive="active"><span class="ic">📅</span>行事曆</a>
      <a routerLink="/memories" routerLinkActive="active"><span class="ic">📷</span>回憶</a>
      <a routerLink="/us" routerLinkActive="active"><span class="ic">💞</span>我們</a>
    </nav>
  `,
})
export class ShellPage implements OnInit {
  private auth = inject(Auth);
  private couple = inject(CoupleStore);

  async ngOnInit() {
    if (!this.auth.user()) {
      try { await this.auth.loadMe(); } catch { /* interceptor/guard handle auth */ }
    }
    if (!this.couple.couple()) {
      try { await this.couple.load(); } catch { /* ignore */ }
    }
  }
}
