package com.pairflow.wishlist.dto;

import com.pairflow.common.enums.Priority;
import com.pairflow.wishlist.WishCategory;
import com.pairflow.wishlist.WishStatus;
import jakarta.validation.constraints.Size;

public record UpdateWishRequest(
        @Size(max = 200) String title,
        @Size(max = 1000) String description,
        WishCategory category,
        Priority priority,
        WishStatus status,
        Double estimatedCost,
        String location,
        String link
) {
}
