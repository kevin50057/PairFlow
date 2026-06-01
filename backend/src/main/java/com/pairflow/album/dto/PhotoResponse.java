package com.pairflow.album.dto;

import com.pairflow.album.Photo;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record PhotoResponse(
        String id,
        String coupleId,
        String albumId,
        String uploaderId,
        String imageUrl,
        String thumbnailUrl,
        String caption,
        Instant takenAt,
        String locationName,
        List<String> tags,
        boolean isFavorite,
        Instant createdAt
) {
    public static PhotoResponse from(Photo p) {
        List<String> tags = (p.getTagsCsv() == null || p.getTagsCsv().isBlank())
                ? List.of()
                : Arrays.stream(p.getTagsCsv().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new PhotoResponse(p.getId(), p.getCoupleId(), p.getAlbumId(), p.getUploaderId(),
                p.getImageUrl(), p.getThumbnailUrl(), p.getCaption(), p.getTakenAt(),
                p.getLocationName(), tags, p.isFavorite(), p.getCreatedAt());
    }
}
