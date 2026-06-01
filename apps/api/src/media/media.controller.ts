import { randomUUID } from 'node:crypto';
import { extname } from 'node:path';
import { Controller, Post, UploadedFile, UseGuards, UseInterceptors } from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { diskStorage } from 'multer';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { ApiException } from '../common/api-error';
import { ALLOWED_IMAGE_MIME, ensureUploadDir, MAX_IMAGE_BYTES, publicUrl } from './storage';

@Controller('media')
@UseGuards(JwtAuthGuard)
export class MediaController {
  @Post('review-images')
  @UseInterceptors(
    FileInterceptor('file', {
      storage: diskStorage({
        destination: (_req, _file, cb) => cb(null, ensureUploadDir()),
        filename: (_req, file, cb) => cb(null, `${randomUUID()}${extname(file.originalname).toLowerCase()}`),
      }),
      limits: { fileSize: MAX_IMAGE_BYTES },
      fileFilter: (_req, file, cb) => cb(null, ALLOWED_IMAGE_MIME.has(file.mimetype)),
    }),
  )
  upload(@UploadedFile() file?: Express.Multer.File) {
    if (!file) throw ApiException.validation('請上傳 jpg / png / webp 圖片，且大小不超過 5MB');
    return { url: publicUrl(file.filename) };
  }
}
