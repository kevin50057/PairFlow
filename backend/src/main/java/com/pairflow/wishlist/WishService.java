package com.pairflow.wishlist;

import com.pairflow.common.error.ApiException;
import com.pairflow.common.enums.Priority;
import com.pairflow.couple.CoupleContext;
import com.pairflow.todo.AssigneeOption;
import com.pairflow.todo.TodoService;
import com.pairflow.todo.TodoType;
import com.pairflow.todo.dto.CreateTodoRequest;
import com.pairflow.todo.dto.TodoResponse;
import com.pairflow.wishlist.dto.CreateWishRequest;
import com.pairflow.wishlist.dto.UpdateWishRequest;
import com.pairflow.wishlist.dto.WishResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class WishService {

    private final WishRepository repository;
    private final TodoService todoService;
    private final CoupleContext coupleContext;

    public WishService(WishRepository repository, TodoService todoService, CoupleContext coupleContext) {
        this.repository = repository;
        this.todoService = todoService;
        this.coupleContext = coupleContext;
    }

    @Transactional(readOnly = true)
    public List<WishResponse> list(WishStatus status, WishCategory category) {
        String coupleId = coupleContext.requireCoupleId();
        List<Wish> wishes;
        if (status != null) {
            wishes = repository.findByCoupleIdAndStatusOrderByCreatedAtDesc(coupleId, status);
        } else if (category != null) {
            wishes = repository.findByCoupleIdAndCategoryOrderByCreatedAtDesc(coupleId, category);
        } else {
            wishes = repository.findByCoupleIdOrderByCreatedAtDesc(coupleId);
        }
        return wishes.stream().map(WishResponse::from).toList();
    }

    @Transactional
    public WishResponse create(CreateWishRequest req) {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();
        Wish w = new Wish();
        w.setCoupleId(coupleId);
        w.setTitle(req.title().trim());
        w.setDescription(req.description());
        w.setCategory(req.category() != null ? req.category() : WishCategory.OTHER);
        w.setPriority(req.priority() != null ? req.priority() : Priority.MEDIUM);
        w.setEstimatedCost(req.estimatedCost());
        w.setLocation(req.location());
        w.setLink(req.link());
        w.setAddedBy(me);
        w.setStatus(WishStatus.ACTIVE);
        return WishResponse.from(repository.save(w));
    }

    @Transactional
    public WishResponse update(String id, UpdateWishRequest req) {
        Wish w = load(id);
        if (req.title() != null) w.setTitle(req.title().trim());
        if (req.description() != null) w.setDescription(req.description());
        if (req.category() != null) w.setCategory(req.category());
        if (req.priority() != null) w.setPriority(req.priority());
        if (req.estimatedCost() != null) w.setEstimatedCost(req.estimatedCost());
        if (req.location() != null) w.setLocation(req.location());
        if (req.link() != null) w.setLink(req.link());
        if (req.status() != null) applyStatus(w, req.status());
        return WishResponse.from(w);
    }

    @Transactional
    public WishResponse complete(String id) {
        Wish w = load(id);
        applyStatus(w, WishStatus.COMPLETED);
        return WishResponse.from(w);
    }

    @Transactional
    public void delete(String id) {
        repository.delete(load(id));
    }

    /** Turn a wish into a shared todo (spec 7.12 "轉成 Todo"). */
    @Transactional
    public TodoResponse toTodo(String id) {
        Wish w = load(id);
        CreateTodoRequest todoReq = new CreateTodoRequest(
                w.getTitle(), w.getDescription(), TodoType.GENERAL, w.getPriority(), AssigneeOption.BOTH,
                null, null, null, null, null, null, null, null, null, null, null);
        TodoResponse todo = todoService.create(todoReq);
        w.setConvertedTodoId(todo.id());
        return todo;
    }

    private void applyStatus(Wish w, WishStatus status) {
        w.setStatus(status);
        w.setCompletedAt(status == WishStatus.COMPLETED ? Instant.now() : null);
    }

    private Wish load(String id) {
        Wish w = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Wish not found"));
        if (!w.getCoupleId().equals(coupleContext.requireCoupleId())) {
            throw ApiException.notFound("Wish not found");
        }
        return w;
    }
}
