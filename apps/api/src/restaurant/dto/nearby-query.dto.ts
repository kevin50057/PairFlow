import { Type } from 'class-transformer';
import { IsInt, IsNumber, Max, Min } from 'class-validator';
import { PaginationQuery } from '../../common/dto/pagination';

export class NearbyQuery extends PaginationQuery {
  @Type(() => Number)
  @IsNumber()
  @Min(-90)
  @Max(90)
  lat!: number;

  @Type(() => Number)
  @IsNumber()
  @Min(-180)
  @Max(180)
  lng!: number;

  // metres; spec default 1000
  @Type(() => Number)
  @IsInt()
  @Min(50)
  @Max(50_000)
  radius = 1000;
}
