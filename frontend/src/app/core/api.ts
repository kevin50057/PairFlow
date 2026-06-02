import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

/** Dev API base. The Spring backend allows CORS from http://localhost:4200. */
export const API_BASE = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class Api {
  private http = inject(HttpClient);

  get<T>(path: string, params?: Record<string, string | number | boolean | undefined>): Promise<T> {
    let httpParams = new HttpParams();
    if (params) {
      for (const [k, v] of Object.entries(params)) {
        if (v !== undefined && v !== null && v !== '') httpParams = httpParams.set(k, String(v));
      }
    }
    return firstValueFrom(this.http.get<T>(API_BASE + path, { params: httpParams }));
  }

  post<T>(path: string, body?: unknown): Promise<T> {
    return firstValueFrom(this.http.post<T>(API_BASE + path, body ?? {}));
  }

  patch<T>(path: string, body?: unknown): Promise<T> {
    return firstValueFrom(this.http.patch<T>(API_BASE + path, body ?? {}));
  }

  put<T>(path: string, body?: unknown): Promise<T> {
    return firstValueFrom(this.http.put<T>(API_BASE + path, body ?? {}));
  }

  del<T>(path: string): Promise<T> {
    return firstValueFrom(this.http.delete<T>(API_BASE + path));
  }

  upload<T>(path: string, form: FormData): Promise<T> {
    return firstValueFrom(this.http.post<T>(API_BASE + path, form));
  }

  /** Absolute URL for a media path returned by the API (e.g. "/api/media/x.jpg"). */
  mediaUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    return path.startsWith('/api') ? 'http://localhost:8080' + path : API_BASE + path;
  }
}
