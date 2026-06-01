package com.pairflow.todo;

import com.pairflow.todo.dto.ChecklistItemResponse;
import com.pairflow.todo.dto.CommentResponse;
import com.pairflow.todo.dto.TodoResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TodoMapper {

    private final TodoChecklistItemRepository checklistRepository;
    private final TodoCommentRepository commentRepository;

    public TodoMapper(TodoChecklistItemRepository checklistRepository, TodoCommentRepository commentRepository) {
        this.checklistRepository = checklistRepository;
        this.commentRepository = commentRepository;
    }

    public TodoResponse toResponse(Todo t, String viewerId, boolean withComments) {
        List<ChecklistItemResponse> checklist = checklistRepository.findByTodoIdOrderBySortOrderAsc(t.getId())
                .stream().map(ChecklistItemResponse::from).toList();
        List<CommentResponse> comments = withComments
                ? commentRepository.findByTodoIdOrderByCreatedAtAsc(t.getId()).stream().map(CommentResponse::from).toList()
                : null;

        return new TodoResponse(
                t.getId(), t.getCoupleId(), t.getTitle(), t.getDescription(),
                t.getType().name(), t.getStatus().name(), t.getPriority().name(),
                relativeAssignee(t, viewerId), t.getAssigneeUserId(), t.isAssignedToBoth(),
                t.getDueDate(), t.getReminderTime(), t.getRepeatRule(),
                t.isSecret(), t.getSecretUnlockAt(),
                t.getRelatedEventId(), t.getRelatedAnniversaryId(),
                t.getGoalTarget(), t.getGoalCurrent(), t.getGoalUnit(),
                checklist, comments,
                t.getCreatedBy(), t.getCreatedAt(), t.getUpdatedAt(), t.getCompletedAt());
    }

    /** In a 2-person couple, any non-null assignee that isn't the viewer is the partner. */
    private String relativeAssignee(Todo t, String viewerId) {
        if (t.isAssignedToBoth()) return "both";
        if (t.getAssigneeUserId() == null) return "unassigned";
        return t.getAssigneeUserId().equals(viewerId) ? "me" : "partner";
    }
}
