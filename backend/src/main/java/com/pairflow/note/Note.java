package com.pairflow.note;

import com.pairflow.common.entity.BaseEntity;
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
@Table(name = "notes", indexes = @Index(name = "idx_note_couple", columnList = "coupleId"))
public class Note extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false)
    private String receiverId;

    @Column(length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private NoteType noteType = NoteType.NOTE;

    private String backgroundStyle;
    private String imageUrl;

    /** When set in the future, the receiver can't see it until this time (spec 7.8). */
    private Instant unlockTime;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;

    private Instant readAt;
}
