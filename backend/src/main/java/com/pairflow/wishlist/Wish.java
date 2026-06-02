package com.pairflow.wishlist;

import com.pairflow.common.entity.BaseEntity;
import com.pairflow.common.enums.Priority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "wishes", indexes = @Index(name = "idx_wish_couple", columnList = "coupleId"))
public class Wish extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WishCategory category = WishCategory.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Priority priority = Priority.MEDIUM;

    private Double estimatedCost;
    private String location;
    private String link;

    @Column(nullable = false)
    private String addedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WishStatus status = WishStatus.ACTIVE;

    /** Set when the wish has been turned into a todo (spec 7.12 "轉成 Todo"). */
    private String convertedTodoId;

    private Instant completedAt;
}
