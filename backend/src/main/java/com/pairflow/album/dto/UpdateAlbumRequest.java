package com.pairflow.album.dto;

import jakarta.validation.constraints.Size;

public record UpdateAlbumRequest(
        @Size(max = 200) String title,
        @Size(max = 1000) String description,
        String coverPhotoUrl
) {
}
