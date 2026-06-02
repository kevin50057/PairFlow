package com.pairflow.wishlist.dto;

import com.pairflow.common.enums.Priority;
import com.pairflow.wishlist.WishCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWishRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        WishCategory category,
        Priority priority,
        Double estimatedCost,
        String location,
        String link
) {
}
