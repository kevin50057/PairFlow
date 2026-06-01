package com.pairflow.todo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoChecklistItemRepository extends JpaRepository<TodoChecklistItem, String> {

    List<TodoChecklistItem> findByTodoIdOrderBySortOrderAsc(String todoId);

    void deleteByTodoId(String todoId);
}
