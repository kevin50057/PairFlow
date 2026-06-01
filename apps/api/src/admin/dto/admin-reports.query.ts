import { IsIn, IsOptional } from 'class-validator';
import { PaginationQuery } from '../../common/dto/pagination';
import { ReportStatus } from '../../common/enums';

export class AdminReportsQuery extends PaginationQuery {
  @IsOptional()
  @IsIn(Object.values(ReportStatus))
  status?: string;
}
