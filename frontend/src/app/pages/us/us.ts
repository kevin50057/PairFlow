import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CoupleStore } from '../../core/couple';
import { Auth } from '../../core/auth';
import { initial } from '../../core/labels';

@Component({
  selector: 'pf-us',
  imports: [RouterLink],
  template: `
    <div class="appbar">
      <h1>我們 <span class="heart-doodle">💞</span></h1>
      <span class="couple-cartoon" style="font-size:2rem">💑</span>
    </div>
    <div class="screen stack">
      <div class="card card-warm center-text">
        @if (couple.couple(); as c) {
          @if (c.daysTogether != null) { <div class="hero-day">在一起第 {{ c.daysTogether }} 天</div> }
          @else { <div class="hero-day">你們的空間</div> }
          <p class="muted small">
            和 {{ c.partner?.displayName }} 的共同空間
            @if (c.relationshipStartDate) { ·　開始於 {{ c.relationshipStartDate }} }
          </p>
        }
      </div>

      <div class="grid2">
        @for (l of links; track l.path) {
          <a class="card" [routerLink]="l.path" style="text-align:center;padding:18px 12px">
            <div style="font-size:1.6rem">{{ l.icon }}</div>
            <div class="small" style="margin-top:6px;font-weight:600">{{ l.label }}</div>
          </a>
        }
      </div>
    </div>
  `,
})
export class UsPage {
  couple = inject(CoupleStore);
  auth = inject(Auth);
  meInitial() { return initial(this.auth.user()?.displayName); }
  partnerInitial() { return initial(this.couple.couple()?.partner?.displayName); }
  links = [
    { path: '/us/wishlist', icon: '🎆', label: '未來一起做的事' },
    { path: '/us/mood', icon: '💗', label: '心情' },
    { path: '/us/notes', icon: '✉️', label: '小紙條' },
    { path: '/us/questions', icon: '❓', label: '每日問答' },
    { path: '/us/dates', icon: '🗺️', label: '約會規劃' },
    { path: '/us/finance', icon: '💰', label: '記帳' },
    { path: '/us/repair', icon: '🕊️', label: '吵架修復' },
    { path: '/us/ai', icon: '🤖', label: 'AI 助手' },
    { path: '/us/notifications', icon: '🔔', label: '通知' },
    { path: '/us/settings', icon: '⚙️', label: '關係設定' },
  ];
}
