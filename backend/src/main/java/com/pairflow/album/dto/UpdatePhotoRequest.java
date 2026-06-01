package com.pairflow.album.dto;

import java.time.Instant;
import java.util.List;

public record UpdatePhotoRequest(
        String albumId,
        String caption,
        Instant takenAt,
        String locationName,
        List<String> tags,
        Boolean isFavorite
) {
}
