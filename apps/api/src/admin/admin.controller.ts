import { Body, Controller, Get, Param, ParseIntPipe, Patch, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { Roles } from '../auth/roles.decorator';
import { RolesGuard } from '../auth/roles.guard';
import { ReportService } from '../report/report.service';
import { ReviewService } from '../review/review.service';
import { AdminReportsQuery } from './dto/admin-reports.query';
import { UpdateReportDto } from './dto/update-report.dto';

@Controller('admin')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles('ADMIN')
export class AdminController {
  constructor(
    private readonly reviews: ReviewService,
    private readonly reports: ReportService,
  ) {}

  @Get('reports')
  listReports(@Query() q: AdminReportsQuery) {
    return this.reports.listForAdmin(q.status, q.page, q.size);
  }

  @Patch('reviews/:reviewId/hide')
  hide(@Param('reviewId', ParseIntPipe) reviewId: number) {
    return this.reviews.setVisibility(reviewId, true);
  }

  @Patch('reviews/:reviewId/restore')
  restore(@Param('reviewId', ParseIntPipe) reviewId: number) {
    return this.reviews.setVisibility(reviewId, false);
  }

  @Patch('reports/:reportId')
  updateReport(@Param('reportId', ParseIntPipe) reportId: number, @Body() dto: UpdateReportDto) {
    return this.reports.updateStatus(reportId, dto.status);
  }
}
