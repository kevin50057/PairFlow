import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { RestaurantReviewController } from './review.controller';
import { ReviewService } from './review.service';
import { ReviewController } from './reviews.controller';

@Module({
  imports: [AuthModule],
  controllers: [RestaurantReviewController, ReviewController],
  providers: [ReviewService],
  exports: [ReviewService],
})
export class ReviewModule {}
