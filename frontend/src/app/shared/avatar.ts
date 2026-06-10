import { Component, Input, signal } from '@angular/core';

/**
 * One person's avatar. `src` may be:
 *   - an image path/URL  → e.g. "/avatars/boy.png"
 *   - an emoji token     → e.g. "emoji:🐻" (rendered as a coloured circle)
 *   - empty              → falls back by `fallback` (boy / girl / neutral 💗)
 * A broken image quietly falls back to the 💑 emoji.
 */
@Component({
  selector: 'pf-avatar',
  template: `
    @if (showEmoji()) {
      <span class="ava-emoji" [style.width.px]="size" [style.height.px]="size"
            [style.fontSize.px]="round(size * 0.56)">{{ emoji() }}</span>
    } @else {
      <img class="ava-one" [style.width.px]="size" [style.height.px]="size"
           [src]="img()" alt="" (error)="broken.set(true)" />
    }
  `,
})
export class Avatar {
  private _src?: string | null;
  @Input() set src(v: string | null | undefined) { this._src = v; this.broken.set(false); }
  get src(): string | null | undefined { return this._src; }

  @Input() fallback: 'boy' | 'girl' | 'neutral' = 'neutral';
  @Input() size = 40;
  broken = signal(false);

  private value(): string {
    const s = (this._src ?? '').trim();
    if (s) return s;
    if (this.fallback === 'boy') return '/avatars/boy.png';
    if (this.fallback === 'girl') return '/avatars/girl.png';
    return 'emoji:💗';
  }
  showEmoji(): boolean { return this.broken() || this.value().startsWith('emoji:'); }
  emoji(): string { const v = this.value(); return v.startsWith('emoji:') ? v.slice(6) : '💑'; }
  img(): string { return this.value(); }
  round(n: number): number { return Math.round(n); }
}
