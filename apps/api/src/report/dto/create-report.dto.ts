import { Transform } from 'class-transformer';
import { IsIn, IsInt, IsOptional, IsString, MaxLength } from 'class-validator';
import { ReportReason } from '../../common/enums';

export class CreateReportDto {
  @IsOptional()
  @IsIn(['REVIEW'])
  targetType: string = 'REVIEW';

  @IsInt()
  targetId!: number;

  @IsIn(Object.values(ReportReason), { message: '不支援的檢舉原因' })
  reasonCode!: string;

  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  @MaxLength(500)
  note?: string;
}
