import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class ReviewTagService {
  constructor(private readonly prisma: PrismaService) {}

  async list() {
    const tags = await this.prisma.reviewTag.findMany({ orderBy: { sortOrder: 'asc' } });
    return tags.map((t) => ({ code: t.code, label: t.label, sortOrder: t.sortOrder }));
  }
}
