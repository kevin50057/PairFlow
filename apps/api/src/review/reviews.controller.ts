import { Body, Controller, Delete, Get, Param, ParseIntPipe, Put, UseGuards } from '@nestjs/common';
import type { AuthUser } from '../auth/auth-types';
import { CurrentUser } from '../auth/current-user.decorator';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CreateReviewDto } from './dto/create-review.dto';
import { ReviewService } from './review.service';

// Owner-scoped operations on a single review.
@Controller('reviews')
@UseGuards(JwtAuthGuard)
export class ReviewController {
  constructor(private readonly service: ReviewService) {}

  @Get(':reviewId')
  getOne(@Param('reviewId', ParseIntPipe) reviewId: number, @CurrentUser() user: AuthUser) {
    return this.service.getForEdit(reviewId, user.id, user.role);
  }

  @Put(':reviewId')
  update(
    @Param('reviewId', ParseIntPipe) reviewId: number,
    @CurrentUser() user: AuthUser,
    @Body() dto: CreateReviewDto,
  ) {
    return this.service.update(reviewId, user.id, dto);
  }

  @Delete(':reviewId')
  remove(@Param('reviewId', ParseIntPipe) reviewId: number, @CurrentUser() user: AuthUser) {
    return this.service.remove(reviewId, user.id);
  }
}
