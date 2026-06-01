package com.pairflow.album;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "photos", indexes = {
        @Index(name = "idx_photo_couple", columnList = "coupleId"),
        @Index(name = "idx_photo_album", columnList = "albumId")
})
public class Photo extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    private String albumId;

    @Column(nullable = false)
    private String uploaderId;

    /** On-disk filename; the binary is served (auth-gated) via /api/media/{storageKey}. */
    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String imageUrl;

    private String thumbnailUrl;

    @Column(length = 500)
    private String caption;

    private Instant takenAt;

    private String locationName;

    @Column(length = 500)
    private String tagsCsv;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;
}
