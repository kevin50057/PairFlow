import { Component, Input, signal } from '@angular/core';

/**
 * The couple's default avatars: the boy + girl cartoon images (public/avatars/).
 * Falls back to the 💑 emoji if the image files aren't present yet.
 */
@Component({
  selector: 'pf-couple-avatar',
  template: `
    @if (failed()) {
      <span class="couple-cartoon" [style.fontSize.px]="size">💑</span>
    } @else {
      <span class="couple-imgs">
        <img class="ava-img" [style.width.px]="size" [style.height.px]="size"
             src="/avatars/boy.png" alt="" (error)="failed.set(true)" />
        <img class="ava-img b" [style.width.px]="size" [style.height.px]="size"
             src="/avatars/girl.png" alt="" (error)="failed.set(true)" />
      </span>
    }
  `,
})
export class CoupleAvatar {
  @Input() size = 36;
  failed = signal(false);
}
