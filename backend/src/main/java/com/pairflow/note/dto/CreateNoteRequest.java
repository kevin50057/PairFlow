package com.pairflow.note.dto;

import com.pairflow.note.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateNoteRequest(
        @Size(max = 200) String title,
        @NotBlank @Size(max = 4000) String content,
        NoteType noteType,
        String backgroundStyle,
        String imageUrl,
        Instant unlockTime
) {
}
