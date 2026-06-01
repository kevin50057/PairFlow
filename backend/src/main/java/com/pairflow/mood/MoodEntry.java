package com.pairflow.mood;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mood_entries", indexes = @Index(name = "idx_mood_couple_user", columnList = "coupleId,userId"))
public class MoodEntry extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MoodType mood;

    private String emoji;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private boolean needResponse = false;
}
