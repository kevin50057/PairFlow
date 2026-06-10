import { Component, Input, inject } from '@angular/core';
import { Auth } from '../core/auth';
import { CoupleStore } from '../core/couple';
import { Avatar } from './avatar';

/**
 * The overlapping pair of avatars shown in app bars: me + my partner.
 * Each person uses their own chosen avatar (profile → 大頭貼); falls back to the
 * boy / girl cartoon defaults when someone hasn't picked one yet.
 */
@Component({
  selector: 'pf-couple-avatar',
  imports: [Avatar],
  template: `
    <span class="couple-imgs">
      <pf-avatar class="ca a" [src]="myAvatar()" [fallback]="myFallback()" [size]="size" />
      <pf-avatar class="ca b" [src]="partnerAvatar()" [fallback]="partnerFallback()" [size]="size" />
    </span>
  `,
})
export class CoupleAvatar {
  private auth = inject(Auth);
  private couple = inject(CoupleStore);
  @Input() size = 36;

  myAvatar() { return this.auth.user()?.avatarUrl; }
  partnerAvatar() { return this.couple.couple()?.partner?.avatarUrl; }
  myFallback(): 'boy' | 'girl' { return this.auth.user()?.gender === 'FEMALE' ? 'girl' : 'boy'; }
  partnerFallback(): 'boy' | 'girl' { return this.couple.couple()?.partner?.gender === 'MALE' ? 'boy' : 'girl'; }
}
