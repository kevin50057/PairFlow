package com.pairflow.todo.dto;

import com.pairflow.todo.TodoChecklistItem;

public record ChecklistItemResponse(String id, String title, boolean completed, int sortOrder) {

    public static ChecklistItemResponse from(TodoChecklistItem item) {
        return new ChecklistItemResponse(item.getId(), item.getTitle(), item.isCompleted(), item.getSortOrder());
    }
}
