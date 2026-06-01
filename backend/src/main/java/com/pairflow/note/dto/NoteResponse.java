package com.pairflow.note.dto;

import com.pairflow.note.Note;

import java.time.Instant;

public record NoteResponse(
        String id,
        String senderId,
        String receiverId,
        String title,
        String content,
        String noteType,
        String backgroundStyle,
        String imageUrl,
        Instant unlockTime,
        boolean locked,
        boolean isRead,
        boolean isFavorite,
        Instant createdAt,
        Instant readAt
) {
    public static NoteResponse from(Note n, Instant now) {
        boolean locked = n.getUnlockTime() != null && n.getUnlockTime().isAfter(now);
        return new NoteResponse(
                n.getId(), n.getSenderId(), n.getReceiverId(), n.getTitle(), n.getContent(),
                n.getNoteType().name(), n.getBackgroundStyle(), n.getImageUrl(), n.getUnlockTime(),
                locked, n.isRead(), n.isFavorite(), n.getCreatedAt(), n.getReadAt());
    }
}
