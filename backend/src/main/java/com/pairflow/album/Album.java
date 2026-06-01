package com.pairflow.album;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "albums", indexes = @Index(name = "idx_album_couple", columnList = "coupleId"))
public class Album extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    private String coverPhotoUrl;

    @Column(nullable = false)
    private String createdBy;
}
