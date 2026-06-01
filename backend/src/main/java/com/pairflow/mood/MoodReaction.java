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
@Table(name = "mood_reactions", indexes = @Index(name = "idx_reaction_mood", columnList = "moodEntryId"))
public class MoodReaction extends BaseEntity {

    @Column(nullable = false)
    private String moodEntryId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReactionType reaction;
}
