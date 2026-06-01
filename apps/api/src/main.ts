import 'reflect-metadata';
import { ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { NestExpressApplication } from '@nestjs/platform-express';
import { AppModule } from './app.module';
import { AllExceptionsFilter } from './common/all-exceptions.filter';
import { buildValidationError } from './common/api-error';
import { ensureUploadDir } from './media/storage';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule, { cors: false });

  app.setGlobalPrefix('api/v1');

  const origins = (process.env.CORS_ORIGIN ?? '').split(',').map((s) => s.trim()).filter(Boolean);
  app.enableCors({ origin: origins.length ? origins : true, credentials: true });

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
      transformOptions: { enableImplicitConversion: true },
      exceptionFactory: (errors) => buildValidationError(errors),
    }),
  );
  app.useGlobalFilters(new AllExceptionsFilter());

  // Serve locally-stored review images at /uploads/<file> (dev storage).
  app.useStaticAssets(ensureUploadDir(), { prefix: '/uploads/' });

  const port = Number(process.env.PORT ?? 3000);
  await app.listen(port);
  // eslint-disable-next-line no-console
  console.log(`🔌 雷評 API listening on http://localhost:${port}/api/v1`);
}

void bootstrap();
