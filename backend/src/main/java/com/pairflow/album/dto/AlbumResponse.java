package com.pairflow.album.dto;

import com.pairflow.album.Album;

import java.time.Instant;

public record AlbumResponse(
        String id,
        String coupleId,
        String title,
        String description,
        String coverPhotoUrl,
        long photoCount,
        String createdBy,
        Instant createdAt
) {
    public static AlbumResponse from(Album a, long photoCount) {
        return new AlbumResponse(a.getId(), a.getCoupleId(), a.getTitle(), a.getDescription(),
                a.getCoverPhotoUrl(), photoCount, a.getCreatedBy(), a.getCreatedAt());
    }
}
