import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { ReportModule } from '../report/report.module';
import { ReviewModule } from '../review/review.module';
import { AdminController } from './admin.controller';

@Module({
  imports: [AuthModule, ReviewModule, ReportModule],
  controllers: [AdminController],
})
export class AdminModule {}
