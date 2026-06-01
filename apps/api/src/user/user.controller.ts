import { Body, Controller, Get, Patch, Query, UseGuards } from '@nestjs/common';
import type { AuthUser } from '../auth/auth-types';
import { CurrentUser } from '../auth/current-user.decorator';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { PaginationQuery } from '../common/dto/pagination';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { UserService } from './user.service';

@Controller('users')
@UseGuards(JwtAuthGuard)
export class UserController {
  constructor(private readonly users: UserService) {}

  @Get('me/reviews')
  myReviews(@CurrentUser() user: AuthUser, @Query() page: PaginationQuery) {
    return this.users.myReviews(user.id, page.page, page.size);
  }

  @Patch('me')
  updateProfile(@CurrentUser() user: AuthUser, @Body() dto: UpdateProfileDto) {
    return this.users.updateProfile(user.id, dto);
  }
}
