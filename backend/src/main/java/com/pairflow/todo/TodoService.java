package com.pairflow.todo;

import com.pairflow.common.enums.Priority;
import com.pairflow.common.error.ApiException;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleContext;
import com.pairflow.notification.NotificationService;
import com.pairflow.notification.NotificationType;
import com.pairflow.todo.dto.AddChecklistItemRequest;
import com.pairflow.todo.dto.AddCommentRequest;
import com.pairflow.todo.dto.ChecklistItemResponse;
import com.pairflow.todo.dto.CommentResponse;
import com.pairflow.todo.dto.CreateTodoRequest;
import com.pairflow.todo.dto.TodoResponse;
import com.pairflow.todo.dto.UpdateChecklistItemRequest;
import com.pairflow.todo.dto.UpdateTodoRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoChecklistItemRepository checklistRepository;
    private final TodoCommentRepository commentRepository;
    private final TodoMapper mapper;
    private final CoupleContext coupleContext;
    private final NotificationService notificationService;

    public TodoService(TodoRepository todoRepository,
                       TodoChecklistItemRepository checklistRepository,
                       TodoCommentRepository commentRepository,
                       TodoMapper mapper,
                       CoupleContext coupleContext,
                       NotificationService notificationService) {
        this.todoRepository = todoRepository;
        this.checklistRepository = checklistRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
        this.coupleContext = coupleContext;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> list(TodoStatus status, TodoType type, AssigneeOption assignee,
                                   Instant dueFrom, Instant dueTo, String keyword, Boolean undated) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);
        Instant now = Instant.now();

        Specification<Todo> spec = Specification
                .where(TodoSpecifications.visibleTo(couple.getId(), me, now))
                .and(TodoSpecifications.status(status))
                .and(TodoSpecifications.type(type))
                .and(TodoSpecifications.assignee(assignee, me, partnerId))
                .and(TodoSpecifications.dueFrom(dueFrom))
                .and(TodoSpecifications.dueTo(dueTo))
                .and(TodoSpecifications.undated(undated))
                .and(TodoSpecifications.keyword(keyword));

        return todoRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(t -> mapper.toResponse(t, me, false)).toList();
    }

    /** Open todos due today — for the home dashboard (spec 22 todayTodos). */
    @Transactional(readOnly = true)
    public List<TodoResponse> todayTodos() {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Instant now = Instant.now();
        LocalDate today = AppTime.today();
        Instant start = today.atStartOfDay(AppTime.ZONE).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(AppTime.ZONE).toInstant();

        Specification<Todo> spec = Specification
                .where(TodoSpecifications.visibleTo(couple.getId(), me, now))
                .and(TodoSpecifications.dueFrom(start))
                .and(TodoSpecifications.dueTo(end));

        return todoRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "dueDate")).stream()
                .filter(t -> t.getStatus() != TodoStatus.DONE && t.getStatus() != TodoStatus.CANCELLED)
                .map(t -> mapper.toResponse(t, me, false))
                .toList();
    }

    @Transactional
    public TodoResponse create(CreateTodoRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);

        Todo t = new Todo();
        t.setCoupleId(couple.getId());
        t.setTitle(req.title().trim());
        t.setDescription(req.description());
        t.setType(req.type() != null ? req.type() : TodoType.GENERAL);
        t.setPriority(req.priority() != null ? req.priority() : Priority.MEDIUM);
        t.setStatus(TodoStatus.PENDING);
        applyAssignee(t, req.assignee(), me, partnerId);
        t.setDueDate(req.dueDate());
        t.setReminderTime(req.reminderTime());
        t.setAutoComplete(Boolean.TRUE.equals(req.autoComplete()) && req.dueDate() != null);
        t.setRepeatRule(req.repeatRule());
        t.setSecret(Boolean.TRUE.equals(req.isSecret()) || t.getType() == TodoType.SURPRISE);
        t.setSecretUnlockAt(req.secretUnlockAt());
        t.setRelatedEventId(req.relatedEventId());
        t.setRelatedAnniversaryId(req.relatedAnniversaryId());
        t.setGoalTarget(req.goalTarget());
        t.setGoalCurrent(req.goalCurrent());
        t.setGoalUnit(req.goalUnit());
        t.setCreatedBy(me);
        t = todoRepository.save(t);

        if (req.checklist() != null) {
            int order = 0;
            for (String title : req.checklist()) {
                if (title == null || title.isBlank()) continue;
                TodoChecklistItem item = new TodoChecklistItem();
                item.setTodoId(t.getId());
                item.setTitle(title.trim());
                item.setSortOrder(order++);
                checklistRepository.save(item);
            }
        }
        if (partnerId.equals(t.getAssigneeUserId())) {
            notificationService.notify(couple.getId(), partnerId, NotificationType.TODO_CREATED,
                    "新任務", "對方建立了「" + t.getTitle() + "」", "TODO", t.getId());
        }
        return mapper.toResponse(t, me, true);
    }

    @Transactional(readOnly = true)
    public TodoResponse get(String id) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Todo t = loadVisible(id, couple.getId(), me);
        return mapper.toResponse(t, me, true);
    }

    @Transactional
    public TodoResponse update(String id, UpdateTodoRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);
        Todo t = loadVisible(id, couple.getId(), me);

        if (req.title() != null) t.setTitle(req.title().trim());
        if (req.description() != null) t.setDescription(req.description());
        if (req.type() != null) t.setType(req.type());
        if (req.priority() != null) t.setPriority(req.priority());
        if (req.assignee() != null) applyAssignee(t, req.assignee(), me, partnerId);
        if (req.dueDate() != null) t.setDueDate(req.dueDate());
        if (req.reminderTime() != null) t.setReminderTime(req.reminderTime());
        if (req.autoComplete() != null) t.setAutoComplete(req.autoComplete());
        if (req.repeatRule() != null) t.setRepeatRule(req.repeatRule());
        if (req.isSecret() != null) t.setSecret(req.isSecret());
        if (req.secretUnlockAt() != null) t.setSecretUnlockAt(req.secretUnlockAt());
        if (req.relatedEventId() != null) t.setRelatedEventId(req.relatedEventId());
        if (req.relatedAnniversaryId() != null) t.setRelatedAnniversaryId(req.relatedAnniversaryId());
        if (req.goalTarget() != null) t.setGoalTarget(req.goalTarget());
        if (req.goalCurrent() != null) t.setGoalCurrent(req.goalCurrent());
        if (req.goalUnit() != null) t.setGoalUnit(req.goalUnit());
        if (req.status() != null) applyStatus(t, req.status());

        return mapper.toResponse(t, me, true);
    }

    @Transactional
    public TodoResponse complete(String id) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Todo t = loadVisible(id, couple.getId(), me);
        applyStatus(t, TodoStatus.DONE);
        notificationService.notify(couple.getId(), couple.partnerOf(me), NotificationType.TODO_COMPLETED,
                "任務完成", "對方完成了「" + t.getTitle() + "」", "TODO", t.getId());
        return mapper.toResponse(t, me, true);
    }

    @Transactional
    public void delete(String id) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Todo t = loadVisible(id, couple.getId(), me);
        checklistRepository.deleteByTodoId(t.getId());
        commentRepository.deleteByTodoId(t.getId());
        todoRepository.delete(t);
    }

    @Transactional
    public CommentResponse addComment(String id, AddCommentRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Todo t = loadVisible(id, couple.getId(), me);
        TodoComment c = new TodoComment();
        c.setTodoId(t.getId());
        c.setAuthorId(me);
        c.setContent(req.content().trim());
        return CommentResponse.from(commentRepository.save(c));
    }

    @Transactional
    public ChecklistItemResponse addChecklistItem(String id, AddChecklistItemRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Todo t = loadVisible(id, couple.getId(), me);
        List<TodoChecklistItem> existing = checklistRepository.findByTodoIdOrderBySortOrderAsc(t.getId());
        int nextOrder = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1;
        TodoChecklistItem item = new TodoChecklistItem();
        item.setTodoId(t.getId());
        item.setTitle(req.title().trim());
        item.setSortOrder(nextOrder);
        return ChecklistItemResponse.from(checklistRepository.save(item));
    }

    @Transactional
    public ChecklistItemResponse updateChecklistItem(String todoId, String itemId, UpdateChecklistItemRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Todo t = loadVisible(todoId, couple.getId(), me);
        TodoChecklistItem item = loadChecklistItem(itemId, t.getId());
        if (req.title() != null) item.setTitle(req.title().trim());
        if (req.isCompleted() != null) item.setCompleted(req.isCompleted());
        return ChecklistItemResponse.from(item);
    }

    @Transactional
    public void deleteChecklistItem(String todoId, String itemId) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Todo t = loadVisible(todoId, couple.getId(), me);
        checklistRepository.delete(loadChecklistItem(itemId, t.getId()));
    }

    // ---- helpers ---------------------------------------------------------

    private void applyAssignee(Todo t, AssigneeOption option, String me, String partnerId) {
        AssigneeOption opt = option != null ? option : AssigneeOption.UNASSIGNED;
        switch (opt) {
            case ME -> {
                t.setAssignedToBoth(false);
                t.setAssigneeUserId(me);
            }
            case PARTNER -> {
                t.setAssignedToBoth(false);
                t.setAssigneeUserId(partnerId);
            }
            case BOTH -> {
                t.setAssignedToBoth(true);
                t.setAssigneeUserId(null);
            }
            case UNASSIGNED -> {
                t.setAssignedToBoth(false);
                t.setAssigneeUserId(null);
            }
        }
    }

    private void applyStatus(Todo t, TodoStatus status) {
        t.setStatus(status);
        if (status == TodoStatus.DONE) {
            if (t.getCompletedAt() == null) t.setCompletedAt(Instant.now());
        } else {
            t.setCompletedAt(null);
        }
    }

    /** Loads a todo, enforcing couple scope and surprise-task visibility (404 if hidden). */
    private Todo loadVisible(String id, String coupleId, String viewerId) {
        Todo t = todoRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Todo not found"));
        if (!t.getCoupleId().equals(coupleId)) {
            throw ApiException.notFound("Todo not found");
        }
        boolean hiddenSecret = t.isSecret()
                && !t.getCreatedBy().equals(viewerId)
                && (t.getSecretUnlockAt() == null || t.getSecretUnlockAt().isAfter(Instant.now()));
        if (hiddenSecret) {
            throw ApiException.notFound("Todo not found");
        }
        return t;
    }

    private TodoChecklistItem loadChecklistItem(String itemId, String todoId) {
        TodoChecklistItem item = checklistRepository.findById(itemId)
                .orElseThrow(() -> ApiException.notFound("Checklist item not found"));
        if (!item.getTodoId().equals(todoId)) {
            throw ApiException.notFound("Checklist item not found");
        }
        return item;
    }
}
