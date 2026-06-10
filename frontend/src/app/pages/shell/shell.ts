import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideCalendarDays, LucideHeartHandshake, LucideHouse, LucideImages, LucideListTodo, LucideUserRound } from '@lucide/angular';
import { Auth } from '../../core/auth';
import { CoupleStore } from '../../core/couple';
import { NotificationStore } from '../../core/notifications';
import { DailyQuestionModal } from '../../shared/daily-question-modal';

@Component({
  selector: 'pf-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    DailyQuestionModal,
    LucideHouse,
    LucideListTodo,
    LucideCalendarDays,
    LucideImages,
    LucideHeartHandshake,
    LucideUserRound,
  ],
  template: `
    <div class="bg-hearts"><span></span><span></span><span></span></div>
    <router-outlet />
    <pf-daily-question />
    <nav class="bottomnav">
      <a routerLink="/home" routerLinkActive="active"><svg class="ic" lucideHouse></svg>首頁</a>
      <a routerLink="/todos" routerLinkActive="active"><svg class="ic" lucideListTodo></svg>任務</a>
      <a routerLink="/calendar" routerLinkActive="active"><svg class="ic" lucideCalendarDays></svg>行事曆</a>
      <a routerLink="/memories" routerLinkActive="active"><svg class="ic" lucideImages></svg>回憶</a>
      <a routerLink="/us" routerLinkActive="active"><svg class="ic" lucideHeartHandshake></svg>我們@if (notif.unread() > 0) { <span class="nav-dot"></span> }</a>
      <a routerLink="/me" routerLinkActive="active"><svg class="ic" lucideUserRound></svg>我</a>
    </nav>
  `,
})
export class ShellPage implements OnInit {
  private auth = inject(Auth);
  private couple = inject(CoupleStore);
  notif = inject(NotificationStore);

  async ngOnInit() {
    if (!this.auth.user()) {
      try { await this.auth.loadMe(); } catch { /* interceptor/guard handle auth */ }
    }
    if (!this.couple.couple()) {
      try { await this.couple.load(); } catch { /* ignore */ }
    }
    this.notif.refresh();
  }
}
