import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../../core/api';
import { Album, Photo } from '../../core/models';

@Component({
  selector: 'pf-memories',
  imports: [FormsModule],
  template: `
    <div class="appbar"><h1>回憶</h1>
      <label class="btn btn-ghost btn-sm">上傳<input type="file" accept="image/*" (change)="upload($event)" hidden /></label>
    </div>
    <div class="screen stack">
      @if (uploading()) { <div class="notice">上傳中…</div> }

      @if (albums().length) {
        <div>
          <div class="section-title">相簿</div>
          <div class="chip-row">
            <span class="chip" [class.active]="!albumFilter()" (click)="filter(null)">全部</span>
            @for (a of albums(); track a.id) {
              <span class="chip" [class.active]="albumFilter() === a.id" (click)="filter(a.id)">{{ a.title }} ({{ a.photoCount }})</span>
            }
          </div>
        </div>
      }

      <div class="row" style="gap:8px">
        <input class="input" placeholder="新增相簿名稱…" name="al" [(ngModel)]="newAlbum" />
        <button class="btn btn-ghost btn-sm" (click)="createAlbum()">建立</button>
      </div>

      <div class="section-title">照片時間軸</div>
      @if (!photos().length) {
        <div class="empty">還沒有照片，上傳第一張一起的回憶吧 📷</div>
      } @else {
        <div class="grid3">
          @for (p of photos(); track p.id) {
            <img class="photo" [src]="media(p.imageUrl)" [alt]="p.caption || ''" loading="lazy" />
          }
        </div>
      }
    </div>
  `,
})
export class MemoriesPage implements OnInit {
  private api = inject(Api);
  albums = signal<Album[]>([]);
  photos = signal<Photo[]>([]);
  albumFilter = signal<string | null>(null);
  uploading = signal(false);
  newAlbum = '';

  ngOnInit() { this.load(); }

  async load() {
    this.albums.set(await this.api.get<Album[]>('/albums'));
    await this.loadPhotos();
  }

  async loadPhotos() {
    const f = this.albumFilter();
    this.photos.set(await this.api.get<Photo[]>('/photos', f ? { albumId: f } : undefined));
  }

  filter(id: string | null) { this.albumFilter.set(id); this.loadPhotos(); }

  async createAlbum() {
    if (!this.newAlbum.trim()) return;
    await this.api.post('/albums', { title: this.newAlbum.trim() });
    this.newAlbum = '';
    this.load();
  }

  async upload(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.uploading.set(true);
    try {
      const form = new FormData();
      form.append('file', file);
      const f = this.albumFilter();
      if (f) form.append('albumId', f);
      await this.api.upload('/photos', form);
      input.value = '';
      await this.load();
    } finally {
      this.uploading.set(false);
    }
  }

  media(path: string) { return this.api.mediaUrl(path); }
}
