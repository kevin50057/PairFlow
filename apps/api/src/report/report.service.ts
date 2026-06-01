import { Injectable } from '@nestjs/common';
import { ApiException } from '../common/api-error';
import { buildPage, Page } from '../common/dto/pagination';
import { ReportStatus } from '../common/enums';
import { PrismaService } from '../prisma/prisma.service';
import { CreateReportDto } from './dto/create-report.dto';

export interface AdminReportItem {
  id: number;
  targetType: string;
  targetId: number;
  reporterUserId: number;
  reasonCode: string;
  note: string | null;
  status: string;
  createdAt: string;
  target: {
    id: number;
    content: string;
    status: string;
    restaurantId: number;
    author: { id: number; nickname: string };
  } | null;
}

@Injectable()
export class ReportService {
  constructor(private readonly prisma: PrismaService) {}

  async create(reporterUserId: number, dto: CreateReportDto) {
    const review = await this.prisma.review.findUnique({
      where: { id: dto.targetId },
      select: { id: true },
    });
    if (!review) throw ApiException.notFound('找不到檢舉對象');

    // One pending report per user per target.
    const existing = await this.prisma.report.findFirst({
      where: {
        targetType: 'REVIEW',
        targetId: dto.targetId,
        reporterUserId,
        status: ReportStatus.PENDING,
      },
    });
    if (existing) throw ApiException.conflict('你已檢舉過這則內容，我們會盡快審核');

    const report = await this.prisma.report.create({
      data: {
        targetType: 'REVIEW',
        targetId: dto.targetId,
        reporterUserId,
        reasonCode: dto.reasonCode,
        note: dto.note ?? null,
        status: ReportStatus.PENDING,
      },
    });
    return { id: report.id, status: report.status, createdAt: report.createdAt.toISOString() };
  }

  async listForAdmin(status: string | undefined, page: number, size: number): Promise<Page<AdminReportItem>> {
    const where = status ? { status } : {};
    const [rows, total] = await this.prisma.$transaction([
      this.prisma.report.findMany({ where, orderBy: { createdAt: 'desc' }, skip: page * size, take: size }),
      this.prisma.report.count({ where }),
    ]);

    const reviewIds = rows.filter((r) => r.targetType === 'REVIEW').map((r) => r.targetId);
    const reviews = await this.prisma.review.findMany({
      where: { id: { in: reviewIds } },
      select: {
        id: true,
        content: true,
        status: true,
        restaurantId: true,
        user: { select: { id: true, nickname: true } },
      },
    });
    const byId = new Map(reviews.map((r) => [r.id, r]));

    const items: AdminReportItem[] = rows.map((r) => {
      const target = byId.get(r.targetId);
      return {
        id: r.id,
        targetType: r.targetType,
        targetId: r.targetId,
        reporterUserId: r.reporterUserId,
        reasonCode: r.reasonCode,
        note: r.note,
        status: r.status,
        createdAt: r.createdAt.toISOString(),
        target: target
          ? {
              id: target.id,
              content: target.content,
              status: target.status,
              restaurantId: target.restaurantId,
              author: { id: target.user.id, nickname: target.user.nickname },
            }
          : null,
      };
    });
    return buildPage(items, page, size, total > (page + 1) * size);
  }

  async updateStatus(reportId: number, status: string) {
    const report = await this.prisma.report.update({ where: { id: reportId }, data: { status } });
    return { id: report.id, status: report.status };
  }
}
