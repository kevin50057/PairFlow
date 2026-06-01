package com.pairflow.todo.dto;

import com.pairflow.todo.TodoComment;

import java.time.Instant;

public record CommentResponse(String id, String authorId, String content, Instant createdAt) {

    public static CommentResponse from(TodoComment c) {
        return new CommentResponse(c.getId(), c.getAuthorId(), c.getContent(), c.getCreatedAt());
    }
}
