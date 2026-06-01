import { IsIn } from 'class-validator';
import { ReportStatus } from '../../common/enums';

export class UpdateReportDto {
  @IsIn([ReportStatus.RESOLVED, ReportStatus.REJECTED, ReportStatus.PENDING])
  status!: string;
}
