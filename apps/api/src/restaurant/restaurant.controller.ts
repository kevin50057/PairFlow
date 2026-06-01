import { Controller, Get, Param, ParseIntPipe, Query } from '@nestjs/common';
import { NearbyQuery } from './dto/nearby-query.dto';
import { SearchQuery } from './dto/search-query.dto';
import { RestaurantService } from './restaurant.service';

@Controller('restaurants')
export class RestaurantController {
  constructor(private readonly service: RestaurantService) {}

  // Static routes are declared before ':id' so they take precedence.
  @Get('nearby')
  nearby(@Query() query: NearbyQuery) {
    return this.service.nearby(query);
  }

  @Get('search')
  search(@Query() query: SearchQuery) {
    return this.service.search(query);
  }

  @Get(':id')
  detail(@Param('id', ParseIntPipe) id: number) {
    return this.service.detail(id);
  }
}
