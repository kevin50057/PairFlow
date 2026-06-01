import { Type } from 'class-transformer';
import { IsInt, Max, Min } from 'class-validator';

/** Shared page/size query params. Extend per-endpoint query DTOs from this. */
export class PaginationQuery {
  @Type(() => Number)
  @IsInt()
  @Min(0)
  page = 0;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(50)
  size = 20;
}

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  hasNext: boolean;
}

export function buildPage<T>(items: T[], page: number, size: number, hasNext: boolean): Page<T> {
  return { items, page, size, hasNext };
}
