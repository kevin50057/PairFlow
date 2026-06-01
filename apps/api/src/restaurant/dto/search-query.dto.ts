import { Transform } from 'class-transformer';
import { IsString, Length } from 'class-validator';
import { PaginationQuery } from '../../common/dto/pagination';

export class SearchQuery extends PaginationQuery {
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  @Length(1, 100)
  q!: string;
}
