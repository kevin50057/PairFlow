import { existsSync, mkdirSync } from 'node:fs';
import { isAbsolute, join } from 'node:path';

/**
 * Local-disk storage helper (the dev implementation of the storage abstraction).
 * In production swap these for an object-storage adapter (S3/GCS) — the
 * controller only depends on `ensureUploadDir()` + `publicUrl()`.
 */
export function uploadDir(): string {
  const dir = process.env.UPLOAD_DIR ?? 'uploads';
  return isAbsolute(dir) ? dir : join(process.cwd(), dir);
}

export function ensureUploadDir(): string {
  const dir = uploadDir();
  if (!existsSync(dir)) mkdirSync(dir, { recursive: true });
  return dir;
}

export function publicUrl(filename: string): string {
  const base = process.env.PUBLIC_BASE_URL ?? 'http://localhost:3000';
  return `${base}/uploads/${filename}`;
}

export const ALLOWED_IMAGE_MIME = new Set(['image/jpeg', 'image/png', 'image/webp']);
export const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
