package com.pairflow.todo;

import com.pairflow.common.error.ApiException;
import com.pairflow.todo.dto.AddChecklistItemRequest;
import com.pairflow.todo.dto.AddCommentRequest;
import com.pairflow.todo.dto.ChecklistItemResponse;
import com.pairflow.todo.dto.CommentResponse;
import com.pairflow.todo.dto.CreateTodoRequest;
import com.pairflow.todo.dto.TodoResponse;
import com.pairflow.todo.dto.UpdateChecklistItemRequest;
import com.pairflow.todo.dto.UpdateTodoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<TodoResponse> list(
            @RequestParam(required = false) TodoStatus status,
            @RequestParam(required = false) TodoType type,
            @RequestParam(required = false) AssigneeOption assignee,
            @RequestParam(required = false) String dueFrom,
            @RequestParam(required = false) String dueTo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean undated) {
        return todoService.list(status, type, assignee, parseInstant(dueFrom), parseInstant(dueTo), keyword, undated);
    }

    @PostMapping
    public TodoResponse create(@Valid @RequestBody CreateTodoRequest req) {
        return todoService.create(req);
    }

    @GetMapping("/{id}")
    public TodoResponse get(@PathVariable String id) {
        return todoService.get(id);
    }

    @PatchMapping("/{id}")
    public TodoResponse update(@PathVariable String id, @Valid @RequestBody UpdateTodoRequest req) {
        return todoService.update(id, req);
    }

    @PostMapping("/{id}/complete")
    public TodoResponse complete(@PathVariable String id) {
        return todoService.complete(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        todoService.delete(id);
    }

    @PostMapping("/{id}/comments")
    public CommentResponse addComment(@PathVariable String id, @Valid @RequestBody AddCommentRequest req) {
        return todoService.addComment(id, req);
    }

    @PostMapping("/{id}/checklist")
    public ChecklistItemResponse addChecklistItem(@PathVariable String id, @Valid @RequestBody AddChecklistItemRequest req) {
        return todoService.addChecklistItem(id, req);
    }

    @PatchMapping("/{id}/checklist/{itemId}")
    public ChecklistItemResponse updateChecklistItem(@PathVariable String id, @PathVariable String itemId,
                                                     @Valid @RequestBody UpdateChecklistItemRequest req) {
        return todoService.updateChecklistItem(id, itemId, req);
    }

    @DeleteMapping("/{id}/checklist/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChecklistItem(@PathVariable String id, @PathVariable String itemId) {
        todoService.deleteChecklistItem(id, itemId);
    }

    /** Accept both offset ("…+08:00") and zulu ("…Z") ISO-8601 instants in query params. */
    private static Instant parseInstant(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return OffsetDateTime.parse(s).toInstant();
        } catch (Exception ignore) {
            // fall through
        }
        try {
            return Instant.parse(s);
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid datetime: " + s);
        }
    }
}
