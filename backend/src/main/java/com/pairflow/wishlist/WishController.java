package com.pairflow.wishlist;

import com.pairflow.todo.dto.TodoResponse;
import com.pairflow.wishlist.dto.CreateWishRequest;
import com.pairflow.wishlist.dto.UpdateWishRequest;
import com.pairflow.wishlist.dto.WishResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/wishes")
public class WishController {

    private final WishService service;

    public WishController(WishService service) {
        this.service = service;
    }

    @GetMapping
    public List<WishResponse> list(@RequestParam(required = false) WishStatus status,
                                   @RequestParam(required = false) WishCategory category) {
        return service.list(status, category);
    }

    @PostMapping
    public WishResponse create(@Valid @RequestBody CreateWishRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}")
    public WishResponse update(@PathVariable String id, @Valid @RequestBody UpdateWishRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/complete")
    public WishResponse complete(@PathVariable String id) {
        return service.complete(id);
    }

    @PostMapping("/{id}/to-todo")
    public TodoResponse toTodo(@PathVariable String id) {
        return service.toTodo(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
