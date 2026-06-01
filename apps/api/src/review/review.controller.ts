import { Body, Controller, Get, Param, ParseIntPipe, Post, Query, UseGuards } from '@nestjs/common';
import type { AuthUser } from '../auth/auth-types';
import { CurrentUser } from '../auth/current-user.decorator';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { PaginationQuery } from '../common/dto/pagination';
import { CreateReviewDto } from './dto/create-review.dto';
import { ReviewService } from './review.service';

@Controller('restaurants/:restaurantId/reviews')
export class RestaurantReviewController {
  constructor(private readonly service: ReviewService) {}

  @Get()
  list(@Param('restaurantId', ParseIntPipe) restaurantId: number, @Query() page: PaginationQuery) {
    return this.service.listByRestaurant(restaurantId, page.page, page.size);
  }

  @Post()
  @UseGuards(JwtAuthGuard)
  create(
    @Param('restaurantId', ParseIntPipe) restaurantId: number,
    @CurrentUser() user: AuthUser,
    @Body() dto: CreateReviewDto,
  ) {
    return this.service.create(restaurantId, user.id, dto);
  }
}
